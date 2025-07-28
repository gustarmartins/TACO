package com.mekki.taco.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mekki.taco.data.db.dao.AlimentoDao
import com.mekki.taco.data.db.dao.DietaDao

class HomeViewModelFactory(
    private val dietaDao: DietaDao,
    private val alimentoDao: AlimentoDao
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(dietaDao, alimentoDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}