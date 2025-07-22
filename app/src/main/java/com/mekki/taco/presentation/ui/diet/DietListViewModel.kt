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

class DietListViewModel(private val dietaDao: DietaDao) : ViewModel() {

    companion object {
        private const val TAG = "DietListViewModel"
    }

    private val _dietas = MutableStateFlow<List<Dieta>>(emptyList())
    val dietas: StateFlow<List<Dieta>> = _dietas.asStateFlow()

    // Poderia ter um estado de loading/erro também, se necessário
    // private val _isLoading = MutableStateFlow(false)
    // val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        Log.d(TAG, "ViewModel inicializado. Buscando todas as dietas...")
        viewModelScope.launch {
            // _isLoading.value = true // Se tiver estado de loading
            dietaDao.buscarTodasDietas()
                .catch { exception ->
                    Log.e(TAG, "Erro ao buscar dietas", exception)
                    // _isLoading.value = false // Se tiver estado de loading
                    // Tratar o erro, talvez emitir um estado de erro para a UI
                }
                .collect { listaDeDietas ->
                    Log.d(TAG, "Dietas recebidas do DAO: ${listaDeDietas.size} itens.")
                    _dietas.value = listaDeDietas
                    // _isLoading.value = false // Se tiver estado de loading
                }
        }
    }

    // Função para adicionar uma nova dieta (exemplo, a lógica de UI para isso virá depois)
    // Esta função seria chamada, por exemplo, após o usuário preencher um formulário e clicar em salvar.
    fun adicionarNovaDieta(nomeDieta: String, objetivoCalorias: Double? = null) {
        viewModelScope.launch {
            val novaDieta = Dieta(
                nome = nomeDieta,
                dataCriacao = System.currentTimeMillis(), // Pega o timestamp atual
                objetivoCalorias = objetivoCalorias
            )
            val idNovaDieta = dietaDao.inserirDieta(novaDieta)
            Log.d(TAG, "Nova dieta inserida com ID: $idNovaDieta e nome: $nomeDieta")
            // O Flow em 'dietas' deve atualizar a lista automaticamente.
        }
    }

    fun deletarDieta(dieta: Dieta){
        viewModelScope.launch {
            dietaDao.deletarDieta(dieta)
            Log.d(TAG, "Dieta deletada: ${dieta.nome}")
        }
    }
}