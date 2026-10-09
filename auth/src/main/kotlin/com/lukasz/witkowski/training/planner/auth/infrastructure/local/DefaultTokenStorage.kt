package com.lukasz.witkowski.training.planner.auth.infrastructure.local

import com.lukasz.witkowski.training.planner.network.AccessToken
import com.lukasz.witkowski.training.planner.network.AuthTokens
import com.lukasz.witkowski.training.planner.network.TokenStorage

class DefaultTokenStorage(
    private val secureTokenStorage: SecureTokenStorage,
): TokenStorage {
    @Volatile
    private var accessToken: AccessToken? = null

    override fun accessToken(): AccessToken? {
        return accessToken
    }

    override suspend fun getTokens(): AuthTokens? {
        return secureTokenStorage.getRefreshToken()?.let { refreshToken ->
            AuthTokens(
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        }
    }

    override suspend fun saveTokens(tokens: AuthTokens) {
        accessToken = tokens.accessToken
        secureTokenStorage.saveRefreshToken(tokens.refreshToken)
    }

    override suspend fun clear() {
        accessToken = null
        secureTokenStorage.clear()
    }
}
