package com.lukasz.witkowski.training.planner.auth.domain

import com.lukasz.witkowski.training.planner.auth.domain.model.User
import com.lukasz.witkowski.training.planner.auth.domain.model.UserFailure
import com.lukasz.witkowski.training.planner.auth.domain.model.WeightUnit
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUserProfile(): AppResult<User, UserFailure>

    val weightUnit: Flow<WeightUnit>

    suspend fun setWeightUnit(weightUnit: WeightUnit)
}
