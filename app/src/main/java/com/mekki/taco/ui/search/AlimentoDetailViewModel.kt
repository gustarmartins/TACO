package com.mekki.taco.ui.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mekki.taco.data.local.AlimentoDao // Ajuste o import
import com.mekki.taco.data.model.Alimento    // Ajuste o import
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AlimentoDetailViewModel(
    private val alimentoId: Int,
    private val alimentoDao: AlimentoDao
) : ViewModel() {

    companion object {
        private const val TAG = "AlimentoDetailVM"
    }

    private val _alimento = MutableStateFlow<Alimento?>(null)
    val alimento: StateFlow<Alimento?> = _alimento.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        Log.d(TAG, "ViewModel inicializado para alimentoId: $alimentoId")
        viewModelScope.launch {
            _isLoading.value = true
            alimentoDao.buscarAlimentoPorId(alimentoId)
                .catch { e ->
                    Log.e(TAG, "Erro ao buscar alimento por ID $alimentoId", e)
                    _isLoading.value = false
                    _alimento.value = null // Ou algum estado de erro
                }
                .collectLatest { fetchedAlimento ->
                    Log.d(TAG, "Alimento recebido do DAO: ${fetchedAlimento?.nome}")
                    _alimento.value = fetchedAlimento
                    _isLoading.value = false
                }
        }
    }
}