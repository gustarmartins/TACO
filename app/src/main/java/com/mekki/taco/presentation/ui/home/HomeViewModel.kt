package com.mekki.taco.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mekki.taco.data.db.dao.AlimentoDao
import com.mekki.taco.data.db.dao.DietaDao
import com.mekki.taco.data.db.entity.Alimento
import com.mekki.taco.data.model.DietaComItens
import com.mekki.taco.presentation.ui.diet.DietTotals
import com.mekki.taco.utils.NutrientCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeState(
    val primaryDiet: DietaComItens? = null,
    val dietTotals: DietTotals = DietTotals(),
    val searchTerm: String = "",
    val searchIsLoading: Boolean = false,
    val searchResults: List<Alimento> = emptyList(),
    val expandedAlimentoId: Int? = null
)

class HomeViewModel(
    private val dietaDao: DietaDao,
    private val alimentoDao: AlimentoDao
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadPrimaryDiet()
        observeSearchTerm()
    }

    private fun loadPrimaryDiet() {
        viewModelScope.launch {
            dietaDao.getLatestDietaWithItems().collect { dietWithItems ->
                _state.update { it.copy(primaryDiet = dietWithItems) }
                dietWithItems?.let { processDietTotals(it) }
            }
        }
    }

    private fun processDietTotals(diet: DietaComItens) {
        var totalKcal = 0.0
        var totalProtein = 0.0
        var totalCarbs = 0.0
        var totalFat = 0.0

        diet.itens.forEach { item ->
            val nutrients = NutrientCalculator.calcularNutrientesParaPorcao(
                alimentoBase = item.alimento,
                quantidadeDesejadaGramas = item.itemDieta.quantidadeGramas
            )
            totalKcal += nutrients.energiaKcal ?: 0.0
            totalProtein += nutrients.proteina ?: 0.0
            totalCarbs += nutrients.carboidratos ?: 0.0
            totalFat += nutrients.lipidios?.total ?: 0.0
        }

        _state.update {
            it.copy(
                dietTotals = DietTotals(
                    totalKcal = totalKcal,
                    totalProtein = totalProtein,
                    totalCarbs = totalCarbs,
                    totalFat = totalFat
                )
            )
        }
    }

    private fun observeSearchTerm() {
        viewModelScope.launch {
            _state.map { it.searchTerm }
                .debounce(300) // Waits for user to stop typing
                .distinctUntilChanged() // Search only if text has changed
                .flatMapLatest { term ->
                    if (term.length < 2) {
                        flowOf(emptyList())
                    } else {
                        _state.update { it.copy(searchIsLoading = true) }
                        alimentoDao.buscarAlimentosPorNome(term)
                    }
                }
                .catch { e ->
                    // Lidar com possiveis erros do banco de dados
                    _state.update { it.copy(searchIsLoading = false) }
                }
                .collect { results ->
                    _state.update {
                        it.copy(
                            searchIsLoading = false,
                            searchResults = results
                        )
                    }
                }
        }
    }

    fun onSearchTermChange(term: String) {
        _state.update { it.copy(searchTerm = term, expandedAlimentoId = null) }
    }

    // This function now toggles the expanded state of a food item
    fun onAlimentoToggled(alimentoId: Int) {
        _state.update {
            if (it.expandedAlimentoId == alimentoId) {
                it.copy(expandedAlimentoId = null) // Collapse if already expanded
            } else {
                it.copy(expandedAlimentoId = alimentoId) // Expand new item
            }
        }
    }

    // limpar a busca
    fun cleanSearch() {
        _state.update {
            it.copy(
                searchTerm = "",
                searchResults = emptyList(),
                expandedAlimentoId = null
            )
        }
    }
}
