package com.mekki.taco.presentation.ui.addfood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mekki.taco.data.db.dao.AlimentoDao
import com.mekki.taco.data.db.dao.ItemDietaDao

/**
 * Factory for creating instances of AddFoodToDietViewModel.
 * This is necessary to pass arguments (DAOs) to the ViewModel's constructor.
 */
class AddFoodToDietViewModelFactory(
    private val alimentoDao: AlimentoDao,
    private val itemDietaDao: ItemDietaDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddFoodToDietViewModel::class.java)) {
            return AddFoodToDietViewModel(alimentoDao, itemDietaDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
