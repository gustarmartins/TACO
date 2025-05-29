package com.mekki.taco.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mekki.taco.data.local.AlimentoDao // Ajuste o import

class AlimentoDetailViewModelFactory(
    private val alimentoId: Int,
    private val alimentoDao: AlimentoDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlimentoDetailViewModel::class.java)) {
            return AlimentoDetailViewModel(alimentoId, alimentoDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class para AlimentoDetailViewModelFactory: ${modelClass.name}")
    }
}