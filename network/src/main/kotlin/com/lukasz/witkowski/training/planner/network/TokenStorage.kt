package com.lukasz.witkowski.training.planner.network

interface TokenStorage {
    fun accessToken(): AccessToken?
    suspend fun getTokens(): AuthTokens?
    suspend fun saveTokens(tokens: AuthTokens)
    suspend fun clear()
}
