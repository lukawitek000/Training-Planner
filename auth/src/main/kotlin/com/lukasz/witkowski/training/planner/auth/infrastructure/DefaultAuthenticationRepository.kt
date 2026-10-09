package com.lukasz.witkowski.training.planner.auth.infrastructure

import com.lukasz.witkowski.training.planner.auth.domain.AuthenticationRepository
import com.lukasz.witkowski.training.planner.auth.domain.model.AuthTokens
import com.lukasz.witkowski.training.planner.auth.domain.model.AuthenticationFailure
import com.lukasz.witkowski.training.planner.auth.domain.model.AuthenticationResult
import com.lukasz.witkowski.training.planner.auth.domain.model.SignInForm
import com.lukasz.witkowski.training.planner.auth.domain.model.SignUpForm
import com.lukasz.witkowski.training.planner.auth.infrastructure.local.TokenStorage
import com.lukasz.witkowski.training.planner.auth.infrastructure.remote.AuthenticationRemoteDataSource
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import com.lukasz.witkowski.training.planner.shared.utils.fold
import com.lukasz.witkowski.training.planner.shared.utils.runCatchingCancellable

class DefaultAuthenticationRepository(
    private val tokenStorage: TokenStorage,
    private val remoteDataSource: AuthenticationRemoteDataSource,
) : AuthenticationRepository {
    override suspend fun refreshAccessToken(): AuthenticationResult {
        val tokens = tokenStorage.getTokens() ?: return AuthenticationResult.Failure(AuthenticationFailure.RefreshTokenNotAvailable)
        return remoteDataSource.refreshTokens(tokens).processResult()
    }

    override suspend fun signUp(form: SignUpForm): AuthenticationResult {
        return remoteDataSource.signUp(form).processResult()
    }

    override suspend fun signIn(form: SignInForm): AuthenticationResult {
        return remoteDataSource.signIn(form).processResult()
    }

    override suspend fun logOut(): AuthenticationResult {
        return runCatchingCancellable {
            tokenStorage.clear()
        }.fold(
            onSuccess = { AuthenticationResult.Success },
            onFailure = { AuthenticationResult.Failure(AuthenticationFailure.UnknownFailure) }
        )
    }

    private suspend fun AppResult<AuthTokens, AuthenticationFailure>.processResult(): AuthenticationResult =
        fold(
            onSuccess = { tokens ->
                tokenStorage.saveTokens(tokens)
                AuthenticationResult.Success
            },
            onError = { failure ->
                AuthenticationResult.Failure(failure)
            },
        )
}
