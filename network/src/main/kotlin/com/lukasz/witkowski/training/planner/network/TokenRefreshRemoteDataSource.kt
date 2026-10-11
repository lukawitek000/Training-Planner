package com.lukasz.witkowski.training.planner.network

import com.lukasz.witkowski.training.planner.shared.utils.AppResult

interface TokenRefreshRemoteDataSource {
    suspend fun refreshTokens(refreshToken: String): AppResult<AuthTokens, TokenRefreshFailure>
}
