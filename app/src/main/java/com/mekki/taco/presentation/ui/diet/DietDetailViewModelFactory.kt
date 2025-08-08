package com.mekki.taco.presentation.ui.diet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mekki.taco.data.db.dao.DietaDao
import com.mekki.taco.data.db.dao.ItemDietaDao

class DietDetailViewModelFactory(
    private val dietId: Int,
    private val dietaDao: DietaDao,
    private val itemDietaDao: ItemDietaDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DietDetailViewModel::class.java)) {
            // Now we pass only the required arguments
            return DietDetailViewModel(dietId, dietaDao, itemDietaDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for DietDetail")
    }
}
