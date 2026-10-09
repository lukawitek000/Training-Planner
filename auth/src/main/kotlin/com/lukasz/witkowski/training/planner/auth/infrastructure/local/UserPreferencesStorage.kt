package com.lukasz.witkowski.training.planner.auth.infrastructure.local

import com.lukasz.witkowski.training.planner.auth.domain.model.WeightUnit
import kotlinx.coroutines.flow.Flow

interface UserPreferencesStorage {
    val weightUnit: Flow<WeightUnit>

    suspend fun setWeightUnit(weightUnit: WeightUnit)
}
