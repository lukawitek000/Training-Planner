package com.lukasz.witkowski.training.planner.user.infrastructure

import com.lukasz.witkowski.training.planner.network.SyncScheduler
import com.lukasz.witkowski.training.planner.network.TokenStorage
import com.lukasz.witkowski.training.planner.shared.featureflags.FeatureFlag
import com.lukasz.witkowski.training.planner.shared.featureflags.FeatureFlagProvider
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import com.lukasz.witkowski.training.planner.user.domain.UserRepository
import com.lukasz.witkowski.training.planner.user.domain.model.User
import com.lukasz.witkowski.training.planner.user.domain.model.UserFailure
import com.lukasz.witkowski.training.planner.user.domain.model.WeightUnit
import com.lukasz.witkowski.training.planner.user.infrastructure.local.UserPreferencesStorage
import com.lukasz.witkowski.training.planner.user.infrastructure.remote.UserRemoteDataSource
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

class DefaultUserRepository(
    private val tokenStorage: TokenStorage,
    private val userRemoteDataSource: UserRemoteDataSource,
    private val userPreferencesStorage: UserPreferencesStorage,
    private val syncScheduler: SyncScheduler? = null,
    private val featureFlagProvider: FeatureFlagProvider? = null
) : UserRepository {
    override suspend fun getUserProfile(): AppResult<User, UserFailure> {
        if (featureFlagProvider?.isEnabled(FeatureFlag.ONLINE_SYNC) == false) {
            Timber.i("getUserProfile: Online sync feature flag disabled, returning NotSignedIn")
            return AppResult.Error(UserFailure.NotSignedIn)
        }
        val refreshToken = tokenStorage.getTokens()?.refreshToken
        if (refreshToken.isNullOrBlank()) {
            Timber.i("getUserProfile: No refresh token present, returning NotSignedIn")
            return AppResult.Error(UserFailure.NotSignedIn)
        }
        Timber.d("getUserProfile: Fetching remote profile")
        return userRemoteDataSource.getUserProfile()
    }

    override val weightUnit: Flow<WeightUnit> = userPreferencesStorage.weightUnit

    override suspend fun setWeightUnit(weightUnit: WeightUnit) {
        Timber.d("setWeightUnit: Setting preferred weight unit to %s", weightUnit)
        userPreferencesStorage.setWeightUnit(weightUnit)
    }

    override val autoSyncEnabled: Flow<Boolean> = userPreferencesStorage.autoSyncEnabled

    override suspend fun setAutoSyncEnabled(enabled: Boolean) {
        Timber.d("setAutoSyncEnabled: Setting auto sync enabled to %s", enabled)
        userPreferencesStorage.setAutoSyncEnabled(enabled)
        if (featureFlagProvider?.isEnabled(FeatureFlag.ONLINE_SYNC) == false) {
            return
        }
        if (enabled) {
            syncScheduler?.enqueueMutationSync()
            syncScheduler?.enqueueRemoteChangeSync(force = true)
        } else {
            syncScheduler?.cancelMutationSync()
        }
    }

    override suspend fun signOut(keepLocalData: Boolean) {
        Timber.i("signOut: Clearing user tokens and sync data (keepLocalData=%s)", keepLocalData)
        tokenStorage.clear()
        syncScheduler?.cancelMutationSync()
        syncScheduler?.clearLocalSyncData(keepLocalData)
    }
}
