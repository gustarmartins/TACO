package com.mekki.taco.presentation.ui.addfood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mekki.taco.data.db.dao.AlimentoDao
import com.mekki.taco.data.db.dao.DietaDao
import com.mekki.taco.data.db.dao.ItemDietaDao
import com.mekki.taco.data.db.entity.Dieta
import com.mekki.taco.data.db.entity.ItemDieta
import com.mekki.taco.data.db.entity.Alimento
import com.mekki.taco.utils.NutrientCalculator
import com.mekki.taco.utils.NutrientesPorPorcao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AddFoodUiState(
    val alimento: Alimento? = null,
    val allDiets: List<Dieta> = emptyList(),
    val portion: String = "100",
    val calculatedNutrients: NutrientesPorPorcao? = null,
    val selectedDiet: Dieta? = null,     // <-- expose the selected diet
    val isLoading: Boolean = true
)

class AddFoodViewModel(
    private val foodId: Int,
    private val alimentoDao: AlimentoDao,
    private val dietaDao: DietaDao,
    private val itemDietaDao: ItemDietaDao,
    private val initialDietId: Int? = null
) : ViewModel() {

    private val _portion = MutableStateFlow("100")
    private val _selectedDietId = MutableStateFlow<Int?>(initialDietId)
    private val _uiState = MutableStateFlow(AddFoodUiState())
    val uiState: StateFlow<AddFoodUiState> = _uiState.asStateFlow()

    init {
        val alimentoFlow: Flow<Alimento?> = alimentoDao.buscarAlimentoPorId(foodId)
        val dietsFlow: Flow<List<Dieta>> = dietaDao.buscarTodasDietas()

        viewModelScope.launch {
            combine(alimentoFlow, dietsFlow, _portion, _selectedDietId) { alimento, diets, portionStr, selectedId ->
                val portionVal = portionStr.toDoubleOrNull() ?: 100.0
                val calculated = alimento?.let { NutrientCalculator.calcularNutrientesParaPorcao(it, portionVal) }
                val selectedDiet = selectedId?.let { id -> diets.find { it.id == id } }
                AddFoodUiState(
                    alimento = alimento,
                    allDiets = diets,
                    portion = portionStr,
                    calculatedNutrients = calculated,
                    selectedDiet = selectedDiet,
                    isLoading = alimento == null
                )
            }.collect {
                _uiState.value = it
            }
        }
    }

    fun onPortionChange(newPortion: String) {
        if (newPortion.matches("^\\d*\\.?\\d*\$".toRegex())) {
            _portion.value = newPortion
        }
    }

    /**
     * Called by UI when user selects a diet from the dropdown.
     */
    fun selectDiet(dietId: Int?) {
        _selectedDietId.value = dietId
    }

    fun addItemToExistingDiet(dietId: Int, mealType: String) {
        viewModelScope.launch {
            val portionValue = _portion.value.toDoubleOrNull() ?: return@launch
            val item = ItemDieta(
                dietaId = dietId,
                alimentoId = foodId,
                quantidadeGramas = portionValue,
                tipoRefeicao = mealType
            )
            itemDietaDao.inserirItemDieta(item)
        }
    }

    fun addItemToNewDiet(dietName: String, mealType: String) {
        viewModelScope.launch {
            val portionValue = _portion.value.toDoubleOrNull() ?: return@launch
            val newDieta = Dieta(
                nome = dietName,
                dataCriacao = System.currentTimeMillis()
            )
            val newDietIdLong = dietaDao.inserirDieta(newDieta)
            val newDietId = newDietIdLong.toInt()

            // Set the newly created diet as selected so UI reflects that
            _selectedDietId.value = newDietId

            val item = ItemDieta(
                dietaId = newDietId,
                alimentoId = foodId,
                quantidadeGramas = portionValue,
                tipoRefeicao = mealType
            )
            itemDietaDao.inserirItemDieta(item)
        }
    }
}