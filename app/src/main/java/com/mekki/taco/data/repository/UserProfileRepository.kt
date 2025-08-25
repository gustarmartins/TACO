package com.mekki.taco.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.mekki.taco.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Cria uma instância do DataStore para o aplicativo
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_profile")

class UserProfileRepository(context: Context) {

    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val WEIGHT = doublePreferencesKey("weight")
        val HEIGHT = doublePreferencesKey("height")
        val AGE = intPreferencesKey("age")
        val SEX = stringPreferencesKey("sex")
        val PROTEIN_GOAL = doublePreferencesKey("protein_goal")
        val CARBS_GOAL = doublePreferencesKey("carbs_goal")
        val FAT_GOAL = doublePreferencesKey("fat_goal")
        val WATER_GOAL = doublePreferencesKey("water_goal")
        val CALORIE_GOAL = doublePreferencesKey("calorie_goal")
    }

    // Expõe um Flow com o perfil do usuário atualizado
    val userProfileFlow: Flow<UserProfile> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            // Lê os valores. Se não existirem, permanecem null, como definido na data class.
            val weight = preferences[PreferencesKeys.WEIGHT]
            val height = preferences[PreferencesKeys.HEIGHT]
            val age = preferences[PreferencesKeys.AGE]
            val sex = preferences[PreferencesKeys.SEX]
            val calorieGoal = preferences[PreferencesKeys.CALORIE_GOAL]

            // Para as metas, se não houver valor salvo, usamos o padrão da data class.
            val proteinGoal =
                preferences[PreferencesKeys.PROTEIN_GOAL] ?: UserProfile().proteinGoalPerKg
            val carbsGoal = preferences[PreferencesKeys.CARBS_GOAL] ?: UserProfile().carbsGoalPerKg
            val fatGoal = preferences[PreferencesKeys.FAT_GOAL] ?: UserProfile().fatGoalPerKg
            val waterGoal =
                preferences[PreferencesKeys.WATER_GOAL] ?: UserProfile().waterGoalPerMlPerKg

            UserProfile(
                weight = weight,
                height = height,
                age = age,
                sex = sex,
                proteinGoalPerKg = proteinGoal,
                carbsGoalPerKg = carbsGoal,
                fatGoalPerKg = fatGoal,
                waterGoalPerMlPerKg = waterGoal,
                calorieGoal = calorieGoal
            )
        }

    suspend fun saveProfile(profile: UserProfile) {
        dataStore.edit { preferences ->
            profile.weight?.let { preferences[PreferencesKeys.WEIGHT] = it }
            profile.height?.let { preferences[PreferencesKeys.HEIGHT] = it }
            profile.age?.let { preferences[PreferencesKeys.AGE] = it }
            profile.sex?.let { preferences[PreferencesKeys.SEX] = it }
            profile.calorieGoal?.let { preferences[PreferencesKeys.CALORIE_GOAL] = it }

            preferences[PreferencesKeys.PROTEIN_GOAL] = profile.proteinGoalPerKg
            preferences[PreferencesKeys.CARBS_GOAL] = profile.carbsGoalPerKg
            preferences[PreferencesKeys.FAT_GOAL] = profile.fatGoalPerKg
            preferences[PreferencesKeys.WATER_GOAL] = profile.waterGoalPerMlPerKg
        }
    }

    suspend fun saveWeight(weight: Double) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEIGHT] = weight
        }
    }
}
