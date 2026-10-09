package com.lukasz.witkowski.training.planner.user.infrastructure

import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import com.lukasz.witkowski.training.planner.user.domain.UserRepository
import com.lukasz.witkowski.training.planner.user.domain.model.User
import com.lukasz.witkowski.training.planner.user.domain.model.UserFailure
import com.lukasz.witkowski.training.planner.user.domain.model.WeightUnit
import com.lukasz.witkowski.training.planner.user.infrastructure.local.UserPreferencesStorage
import com.lukasz.witkowski.training.planner.user.infrastructure.remote.UserRemoteDataSource
import kotlinx.coroutines.flow.Flow

class DefaultUserRepository(
    private val userRemoteDataSource: UserRemoteDataSource,
    private val userPreferencesStorage: UserPreferencesStorage,
) : UserRepository {

    override suspend fun getUserProfile(): AppResult<User, UserFailure> {
        return userRemoteDataSource.getUserProfile()
    }

    override val weightUnit: Flow<WeightUnit> = userPreferencesStorage.weightUnit

    override suspend fun setWeightUnit(weightUnit: WeightUnit) {
        userPreferencesStorage.setWeightUnit(weightUnit)
    }
}
