// Arquivo: com/mekki/taco/viewmodel/DietListViewModelFactory.kt
package com.mekki.taco.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mekki.taco.data.db.dao.DietaDao // Importe seu DietaDao

class DietListViewModelFactory(
    private val dietaDao: DietaDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DietListViewModel::class.java)) {
            return DietListViewModel(dietaDao, itemDietaDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for DietList")
    }
}