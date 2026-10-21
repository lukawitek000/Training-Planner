package com.lukasz.witkowski.training.planner.user.domain

import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import com.lukasz.witkowski.training.planner.user.domain.model.User
import com.lukasz.witkowski.training.planner.user.domain.model.UserFailure
import com.lukasz.witkowski.training.planner.user.domain.model.WeightUnit
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUserProfile(): AppResult<User, UserFailure>

    val weightUnit: Flow<WeightUnit>

    suspend fun setWeightUnit(weightUnit: WeightUnit)

    val autoSyncEnabled: Flow<Boolean>

    suspend fun setAutoSyncEnabled(enabled: Boolean)

    suspend fun signOut(keepLocalData: Boolean = false)
}
