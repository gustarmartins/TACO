package com.mekki.taco.ui.search // Conforme sua especificação

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mekki.taco.data.db.dao.AlimentoDao
import com.mekki.taco.data.db.entity.Alimento
import com.mekki.taco.util.NutrientCalculator // Importe seu NutrientCalculator
import com.mekki.taco.util.NutrientesPorPorcao // Importe a data class
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AlimentoDetailViewModel(
    private val alimentoId: Int,
    private val alimentoDao: AlimentoDao
) : ViewModel() {

    companion object {
        private const val TAG = "AlimentoDetailVM"
    }

    // Armazena o alimento base (valores por 100g)
    private val _alimentoBase = MutableStateFlow<Alimento?>(null)
    // val alimentoBase: StateFlow<Alimento?> = _alimentoBase.asStateFlow() // Pode ser útil expor

    // Quantidade em gramas selecionada pelo usuário
    private val _quantidadeSelecionadaGramas = MutableStateFlow(100.0)
    val quantidadeSelecionadaGramas: StateFlow<Double> = _quantidadeSelecionadaGramas.asStateFlow()

    // Estado de carregamento do alimento base
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Nutrientes calculados para a porção selecionada
    val nutrientesParaPorcao: StateFlow<NutrientesPorPorcao?> =
        combine(_alimentoBase, _quantidadeSelecionadaGramas) { alimento, quantidade ->
            alimento?.let {
                NutrientCalculator.calcularNutrientesParaPorcao(it, quantidade)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        Log.d(TAG, "ViewModel inicializado para alimentoId: $alimentoId")
        viewModelScope.launch {
            _isLoading.value = true
            alimentoDao.buscarAlimentoPorId(alimentoId)
                .catch { e ->
                    Log.e(TAG, "Erro ao buscar alimento por ID $alimentoId", e)
                    _isLoading.value = false
                    _alimentoBase.value = null
                }
                .collectLatest { fetchedAlimento ->
                    Log.d(TAG, "Alimento base (${fetchedAlimento?.nome}) recebido do DAO.")
                    _alimentoBase.value = fetchedAlimento
                    _isLoading.value = false // O carregamento inicial do alimento base terminou
                }
        }
    }

    fun atualizarQuantidade(novaQuantidadeInput: String) {
        val novaQuantidade = novaQuantidadeInput.toDoubleOrNull()
        if (novaQuantidade != null && novaQuantidade > 0) {
            _quantidadeSelecionadaGramas.value = novaQuantidade
            Log.d(TAG, "Quantidade atualizada para: $novaQuantidade g")
        } else {
            Log.d(TAG, "Tentativa de atualizar para quantidade inválida: $novaQuantidadeInput")
            // Opcional: resetar para 100g ou manter o valor anterior se a entrada for inválida
            _quantidadeSelecionadaGramas.value = 100.0 // Exemplo de reset
        }
    }
}