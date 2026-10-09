package com.lukasz.witkowski.training.planner.auth.infrastructure.local

interface SecureTokenStorage {
    suspend fun getRefreshToken(): String?

    suspend fun saveRefreshToken(token: String)

    suspend fun clear()
}