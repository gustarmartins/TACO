package com.mekki.taco.presentation.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mekki.taco.data.model.UserProfile
import com.mekki.taco.data.repository.UserProfileRepository
import com.mekki.taco.utils.BMRCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Data class para o estado da UI, incluindo os valores calculados
data class ProfileUiState(
    val userProfile: UserProfile = UserProfile(),
    val tmb: Double = 0.0, // Taxa Metabólica Basal
    val tdee: Double = 0.0, // Gasto Calórico Diário Total
    val activityLevel: ActivityLevel = ActivityLevel.SEDENTARY
)

// Enum para os níveis de atividade
enum class ActivityLevel(val multiplier: Double, val displayName: String) {
    SEDENTARY(1.2, "Sedentário"),
    LIGHT(1.375, "Leve (1-3 dias/semana)"),
    MODERATE(1.55, "Moderado (3-5 dias/semana)"),
    ACTIVE(1.725, "Ativo (6-7 dias/semana)"),
    VERY_ACTIVE(1.9, "Muito Ativo (trabalho físico)")
}

class ProfileViewModel(
    private val repository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.userProfileFlow.collect { profile ->
                _uiState.update { currentState ->
                    val tmb = BMRCalculator.calculateBMR(profile)
                    val tdee = tmb * currentState.activityLevel.multiplier
                    currentState.copy(userProfile = profile, tmb = tmb, tdee = tdee)
                }
            }
        }
    }

    fun onWeightChange(weight: String) {
        val newWeight = weight.toDoubleOrNull()
        if (newWeight != null) {
            updateProfile(_uiState.value.userProfile.copy(weight = newWeight))
        }
    }

    fun onHeightChange(height: String) {
        val newHeight = height.toDoubleOrNull()
        if (newHeight != null) {
            updateProfile(_uiState.value.userProfile.copy(height = newHeight))
        }
    }

    fun onAgeChange(age: String) {
        val newAge = age.toIntOrNull()
        updateProfile(_uiState.value.userProfile.copy(age = newAge))
    }

    fun onSexChange(sex: String) {
        updateProfile(_uiState.value.userProfile.copy(sex = sex))
    }

    fun onActivityLevelChange(level: ActivityLevel) {
        val tdee = _uiState.value.tmb * level.multiplier
        _uiState.update { it.copy(activityLevel = level, tdee = tdee) }
    }

    private fun updateProfile(newProfile: UserProfile) {
        val tmb = BMRCalculator.calculateBMR(newProfile)
        val tdee = tmb * _uiState.value.activityLevel.multiplier
        _uiState.update { it.copy(userProfile = newProfile, tmb = tmb, tdee = tdee) }
    }

    fun saveProfile() {
        viewModelScope.launch {
            // recebe o objeto de perfil inteiro
            repository.saveProfile(_uiState.value.userProfile)
        }
    }
}
