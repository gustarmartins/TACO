package com.mekki.taco.presentation.ui.addfood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mekki.taco.data.db.dao.AlimentoDao
import com.mekki.taco.data.db.dao.ItemDietaDao
import com.mekki.taco.data.db.entity.Alimento
import com.mekki.taco.data.db.entity.ItemDieta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the AddFoodToDietScreen.
 *
 * @param alimentoDao DAO for accessing Alimento data.
 * @param itemDietaDao DAO for accessing ItemDieta data.
 */
class AddFoodToDietViewModel(
    private val alimentoDao: AlimentoDao,
    private val itemDietaDao: ItemDietaDao
) : ViewModel() {

    // Holds the details of the food item being added.
    private val _alimento = MutableStateFlow<Alimento?>(null)
    val alimento: StateFlow<Alimento?> = _alimento.asStateFlow()

    /**
     * Fetches the details of a specific food item from the database.
     *
     * @param foodId The ID of the food to fetch.
     */
    fun loadAlimento(foodId: Int) {
        viewModelScope.launch {
            // Since the DAO returns a Flow, we collect the first value.
            alimentoDao.buscarAlimentoPorId(foodId).collect {
                _alimento.value = it
            }
        }
    }

    /**
     * Adds a food item to a diet.
     *
     * @param dietId The ID of the diet.
     * @param foodId The ID of the food.
     * @param quantity The quantity of the food in grams.
     * @param mealType The type of meal (e.g., "Breakfast", "Lunch").
     * @param onFoodAdded A callback to be invoked after the food has been successfully added.
     */
    fun addFoodToDiet(dietId: Int, foodId: Int, quantity: Double, mealType: String, onFoodAdded: () -> Unit) {
        viewModelScope.launch {
            val newItem = ItemDieta(
                dietaId = dietId,
                alimentoId = foodId,
                quantidadeGramas = quantity, // Corrected field name
                tipoRefeicao = mealType
            )
            itemDietaDao.inserirItemDieta(newItem) // Assuming this is the insert method name
            onFoodAdded() // Trigger the callback for navigation
        }
    }
}
