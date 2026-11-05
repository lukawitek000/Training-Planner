package com.lukasz.witkowski.training.planner.auth.infrastructure.local

import com.lukasz.witkowski.training.planner.network.AccessToken
import com.lukasz.witkowski.training.planner.network.AuthSessionProvider
import com.lukasz.witkowski.training.planner.network.AuthTokens
import com.lukasz.witkowski.training.planner.network.TokenStorage
import com.lukasz.witkowski.training.planner.shared.featureflags.DefaultFeatureFlagProvider
import com.lukasz.witkowski.training.planner.shared.featureflags.FeatureFlag
import com.lukasz.witkowski.training.planner.shared.featureflags.FeatureFlagProvider

class DefaultTokenStorage(
    private val secureTokenStorage: SecureTokenStorage,
    private val featureFlagProvider: FeatureFlagProvider = DefaultFeatureFlagProvider()
) : TokenStorage,
    AuthSessionProvider {
    @Volatile
    private var accessToken: AccessToken? = null

    override fun accessToken(): AccessToken? = accessToken

    override suspend fun getTokens(): AuthTokens? =
        secureTokenStorage.getRefreshToken()?.let { refreshToken ->
            AuthTokens(
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        }

    override suspend fun saveTokens(tokens: AuthTokens) {
        accessToken = tokens.accessToken
        secureTokenStorage.saveRefreshToken(tokens.refreshToken)
    }

    override suspend fun clear() {
        accessToken = null
        secureTokenStorage.clear()
    }

    override suspend fun isLoggedIn(): Boolean =
        featureFlagProvider.isEnabled(FeatureFlag.ONLINE_SYNC) &&
            secureTokenStorage.getRefreshToken() != null
}
