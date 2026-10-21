package com.lukasz.witkowski.training.planner.network

import kotlinx.coroutines.flow.Flow

interface SyncPreferencesStorage {
    val autoSyncEnabled: Flow<Boolean>

    suspend fun setAutoSyncEnabled(enabled: Boolean)
}
