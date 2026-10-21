package com.lukasz.witkowski.training.planner.user.infrastructure.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lukasz.witkowski.training.planner.network.SyncPreferencesStorage
import com.lukasz.witkowski.training.planner.user.domain.model.WeightUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

private val Context.userPreferencesDataStore by preferencesDataStore("user_preferences")

class AndroidUserPreferencesStorage(private val context: Context) :
    UserPreferencesStorage,
    SyncPreferencesStorage {
    private val weightUnitKey = stringPreferencesKey(WEIGHT_UNIT_KEY)
    private val autoSyncEnabledKey = booleanPreferencesKey(AUTO_SYNC_ENABLED_KEY)

    override val weightUnit: Flow<WeightUnit> =
        context.userPreferencesDataStore.data.map { preferences ->
            preferences[weightUnitKey]?.let { name ->
                runCatching { WeightUnit.valueOf(name) }.getOrNull()
            } ?: WeightUnit.KG
        }

    override suspend fun setWeightUnit(weightUnit: WeightUnit) {
        Timber.d("Updating preferred weight unit in DataStore to: %s", weightUnit)
        context.userPreferencesDataStore.edit { preferences ->
            preferences[weightUnitKey] = weightUnit.name
        }
    }

    override val autoSyncEnabled: Flow<Boolean> =
        context.userPreferencesDataStore.data.map { preferences ->
            preferences[autoSyncEnabledKey] ?: true
        }

    override suspend fun setAutoSyncEnabled(enabled: Boolean) {
        Timber.d("Updating auto sync enabled in DataStore to: %s", enabled)
        context.userPreferencesDataStore.edit { preferences ->
            preferences[autoSyncEnabledKey] = enabled
        }
    }

    private companion object {
        const val WEIGHT_UNIT_KEY = "weight_unit_key"
        const val AUTO_SYNC_ENABLED_KEY = "auto_sync_enabled_key"
    }
}
