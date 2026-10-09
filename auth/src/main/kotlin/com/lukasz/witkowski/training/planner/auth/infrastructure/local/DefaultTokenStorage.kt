package com.lukasz.witkowski.training.planner.auth.infrastructure.local

import com.lukasz.witkowski.training.planner.auth.domain.model.AccessToken
import com.lukasz.witkowski.training.planner.auth.domain.model.AuthTokens

class DefaultTokenStorage(
    private val secureTokenStorage: SecureTokenStorage,
): TokenStorage {
    @Volatile
    private var accessToken: AccessToken? = null
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