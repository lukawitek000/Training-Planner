package com.lukasz.witkowski.training.planner.auth.infrastructure.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lukasz.witkowski.training.planner.auth.domain.model.WeightUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore by preferencesDataStore("user_preferences")

class AndroidUserPreferencesStorage(
    private val context: Context,
) : UserPreferencesStorage {

    private val weightUnitKey = stringPreferencesKey(WEIGHT_UNIT_KEY)

    override val weightUnit: Flow<WeightUnit> = context.userPreferencesDataStore.data.map { preferences ->
        preferences[weightUnitKey]?.let { name ->
            runCatching { WeightUnit.valueOf(name) }.getOrNull()
        } ?: WeightUnit.KG
    }

    override suspend fun setWeightUnit(weightUnit: WeightUnit) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[weightUnitKey] = weightUnit.name
        }
    }

    private companion object {
        const val WEIGHT_UNIT_KEY = "weight_unit_key"
    }
}
