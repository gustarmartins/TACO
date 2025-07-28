// File: app/src/main/java/com/mekki/taco/presentation/ui/diet/DietDetailViewModel.kt

package com.mekki.taco.presentation.ui.diet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mekki.taco.data.db.dao.DietaDao
import com.mekki.taco.data.model.DietaComItens
import com.mekki.taco.data.model.ItemDietaComAlimento
import com.mekki.taco.utils.NutrientCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

// A new data class to hold the calculated nutrient totals
data class DietTotals(
    val totalKcal: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFat: Double = 0.0
)

class DietDetailViewModel(
    private val dietId: Int,
    private val dietaDao: DietaDao
) : ViewModel() {

    // Holds the full diet details, including its name and goal
    private val _dietDetails = MutableStateFlow<DietaComItens?>(null)
    val dietDetails = _dietDetails.asStateFlow()

    // Holds the food items grouped by meal type (e.g., "Café da Manhã")
    private val _groupedItems = MutableStateFlow<Map<String, List<ItemDietaComAlimento>>>(emptyMap())
    val groupedItems = _groupedItems.asStateFlow()

    // Holds the calculated totals for the entire diet
    private val _dietTotals = MutableStateFlow(DietTotals())
    val dietTotals = _dietTotals.asStateFlow()

    init {
        loadDietDetails()
    }

    private fun loadDietDetails() {
        viewModelScope.launch {
            dietaDao.getDietaComItens(dietId).collect { dietaComItens ->
                _dietDetails.value = dietaComItens
                dietaComItens?.let {
                    processDietItems(it.itens)
                }
            }
        }
    }

    private fun processDietItems(items: List<ItemDietaComAlimento>) {
        // Group items by meal type
        _groupedItems.value = items.groupBy { it.itemDieta.tipoRefeicao ?: "Sem Categoria" }

        // Calculate totals
        var totalKcal = 0.0
        var totalProtein = 0.0
        var totalCarbs = 0.0
        var totalFat = 0.0

        items.forEach { item ->
            val nutrients = NutrientCalculator.calcularNutrientesParaPorcao(
                alimentoBase = item.alimento,
                quantidadeDesejadaGramas = item.itemDieta.quantidadeGramas
            )
            totalKcal += nutrients.energiaKcal ?: 0.0
            totalProtein += nutrients.proteina ?: 0.0
            totalCarbs += nutrients.carboidratos ?: 0.0
            totalFat += nutrients.lipidios?.total ?: 0.0
        }

        _dietTotals.value = DietTotals(
            totalKcal = totalKcal,
            totalProtein = totalProtein,
            totalCarbs = totalCarbs,
            totalFat = totalFat
        )
    }
}
