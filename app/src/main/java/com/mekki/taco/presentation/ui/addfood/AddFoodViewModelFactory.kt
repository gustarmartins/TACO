package com.mekki.taco.presentation.ui.addfood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mekki.taco.data.db.dao.AlimentoDao
import com.mekki.taco.data.db.dao.DietaDao
import com.mekki.taco.data.db.dao.ItemDietaDao

class AddFoodViewModelFactory(
    private val foodId: Int,
    private val alimentoDao: AlimentoDao,
    private val dietaDao: DietaDao,
    private val itemDietaDao: ItemDietaDao,
    private val initialDietId: Int? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddFoodViewModel::class.java)) {
            return AddFoodViewModel(
                foodId = foodId,
                alimentoDao = alimentoDao,
                dietaDao = dietaDao,
                itemDietaDao = itemDietaDao,
                initialDietId = initialDietId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
