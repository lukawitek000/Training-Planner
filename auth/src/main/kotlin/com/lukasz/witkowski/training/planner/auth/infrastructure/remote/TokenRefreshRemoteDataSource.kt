package com.lukasz.witkowski.training.planner.auth.infrastructure.remote

import com.lukasz.witkowski.training.planner.auth.domain.model.AuthTokens
import com.lukasz.witkowski.training.planner.auth.domain.model.AuthenticationFailure
import com.lukasz.witkowski.training.planner.shared.utils.AppResult

interface TokenRefreshRemoteDataSource {
    suspend fun refreshTokens(refreshToken: String): AppResult<AuthTokens, AuthenticationFailure>
}
