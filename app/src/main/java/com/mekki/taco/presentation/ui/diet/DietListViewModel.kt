package com.mekki.taco.presentation.ui.diet

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mekki.taco.data.db.dao.DietaDao
import com.mekki.taco.data.db.dao.ItemDietaDao
import com.mekki.taco.data.db.entity.Dieta
import com.mekki.taco.data.db.entity.ItemDieta
import com.mekki.taco.data.model.ItemDietaComAlimento
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DietListViewModel(
    private val dietaDao: DietaDao,
    private val itemDietaDao: ItemDietaDao
) : ViewModel() {

    companion object {
        private const val TAG = "DietListViewModel"
    }

    private val _dietas = MutableStateFlow<List<Dieta>>(emptyList())
    val dietas: StateFlow<List<Dieta>> = _dietas.asStateFlow()

    // --- State for the CreateDietScreen ---
    private val _nomeNovaDieta = MutableStateFlow("")
    val nomeNovaDieta: StateFlow<String> = _nomeNovaDieta.asStateFlow()

    // Holds the list of foods added before the diet is saved
    private val _temporaryFoodList = MutableStateFlow<List<ItemDietaComAlimento>>(emptyList())
    val temporaryFoodList: StateFlow<List<ItemDietaComAlimento>> = _temporaryFoodList.asStateFlow()

    private val _dietaSalvaEvent = MutableSharedFlow<Unit>()
    val dietaSalvaEvent: SharedFlow<Unit> = _dietaSalvaEvent.asSharedFlow()

    init {
        carregarDietas()
    }

    private fun carregarDietas() {
        viewModelScope.launch {
            dietaDao.buscarTodasDietas()
                .catch { e -> Log.e(TAG, "Error loading diets", e) }
                .collect { _dietas.value = it }
        }
    }

    fun onNomeNovaDietaChange(nome: String) {
        _nomeNovaDieta.value = nome
    }

    // --- Functions for the new Create Diet flow ---

    fun addFoodToTemporaryList(item: ItemDietaComAlimento) {
        _temporaryFoodList.value += item
    }

    fun removeTemporaryFoodItem(item: ItemDietaComAlimento) {
        _temporaryFoodList.value = _temporaryFoodList.value.filter { it.itemDieta.id != item.itemDieta.id }
    }

    fun salvarNovaDieta() {
        viewModelScope.launch {
            val nomeLimpo = _nomeNovaDieta.value.trim()
            if (nomeLimpo.isEmpty() || _temporaryFoodList.value.isEmpty()) {
                Log.w(TAG, "Attempted to save diet with empty name or food list.")
                return@launch
            }

            // 1. Create and save the Dieta to get its ID
            val novaDieta = Dieta(
                nome = nomeLimpo,
                dataCriacao = System.currentTimeMillis(),
                objetivoCalorias = null // Removed as requested
            )

            try {
                val newDietId = dietaDao.inserirDieta(novaDieta) // Assume inserirDieta returns the new ID (Long)

                // 2. Create ItemDieta entities linked to the new diet
                val itemsParaSalvar = _temporaryFoodList.value.map { tempItem ->
                    ItemDieta(
                        dietaId = newDietId.toInt(),
                        alimentoId = tempItem.alimento.id,
                        quantidadeGramas = tempItem.itemDieta.quantidadeGramas,
                        tipoRefeicao = tempItem.itemDieta.tipoRefeicao
                    )
                }

                // 3. Save all the items
                itemDietaDao.insertAll(itemsParaSalvar) // You will need to add this function to your DAO

                Log.d(TAG, "New diet '$nomeLimpo' and ${itemsParaSalvar.size} items saved successfully.")
                _dietaSalvaEvent.emit(Unit) // Navigate back

                // 4. Clear the state for the next use
                _nomeNovaDieta.value = ""
                _temporaryFoodList.value = emptyList()

            } catch (e: Exception) {
                Log.e(TAG, "Error saving new diet and its items", e)
            }
        }
    }

    fun deletarDieta(dieta: Dieta) {
        viewModelScope.launch {
            dietaDao.deletarDieta(dieta)
        }
    }
}
