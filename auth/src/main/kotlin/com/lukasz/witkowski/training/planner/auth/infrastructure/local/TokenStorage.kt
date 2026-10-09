package com.lukasz.witkowski.training.planner.auth.infrastructure.local

import com.lukasz.witkowski.training.planner.auth.domain.model.AccessToken
import com.lukasz.witkowski.training.planner.auth.domain.model.AuthTokens

interface TokenStorage {
    fun accessToken(): AccessToken?
    suspend fun getTokens(): AuthTokens?
    suspend fun saveTokens(tokens: AuthTokens)
    suspend fun clear()
}