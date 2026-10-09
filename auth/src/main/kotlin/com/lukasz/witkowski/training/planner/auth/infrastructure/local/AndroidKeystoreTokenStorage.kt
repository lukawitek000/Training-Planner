package com.lukasz.witkowski.training.planner.auth.infrastructure.local

import com.lukasz.witkowski.training.planner.auth.domain.model.AuthTokens
import timber.log.Timber

class AndroidKeystoreTokenStorage(): TokenStorage {
    private var tokens: AuthTokens? = null
    override suspend fun getTokens(): AuthTokens? {
        return tokens.also {
            Timber.i("Read tokens $it")
        }
    }

    override suspend fun saveTokens(tokens: AuthTokens) {
        Timber.i("Save tokens $tokens")
        this.tokens = tokens
    }

    override suspend fun clear() {
        Timber.i("Clear tokens")
        tokens = null
    }
}