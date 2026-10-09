package com.lukasz.witkowski.training.planner.auth.infrastructure

import com.lukasz.witkowski.training.planner.auth.domain.UserRepository
import com.lukasz.witkowski.training.planner.auth.domain.model.User
import com.lukasz.witkowski.training.planner.auth.domain.model.UserFailure
import com.lukasz.witkowski.training.planner.auth.domain.model.WeightUnit
import com.lukasz.witkowski.training.planner.auth.infrastructure.local.TokenStorage
import com.lukasz.witkowski.training.planner.auth.infrastructure.local.UserPreferencesStorage
import com.lukasz.witkowski.training.planner.auth.infrastructure.remote.UserRemoteDataSource
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import kotlinx.coroutines.flow.Flow

class DefaultUserRepository(
    private val tokenStorage: TokenStorage,
    private val userRemoteDataSource: UserRemoteDataSource,
    private val userPreferencesStorage: UserPreferencesStorage,
) : UserRepository {

    override suspend fun getUserProfile(): AppResult<User, UserFailure> {
        val accessToken = tokenStorage.getTokens()?.accessToken?.token
            ?: return AppResult.Error(UserFailure.Unauthorized)
        return userRemoteDataSource.getUserProfile(accessToken)
    }

    override val weightUnit: Flow<WeightUnit> = userPreferencesStorage.weightUnit

    override suspend fun setWeightUnit(weightUnit: WeightUnit) {
        userPreferencesStorage.setWeightUnit(weightUnit)
    }
}
