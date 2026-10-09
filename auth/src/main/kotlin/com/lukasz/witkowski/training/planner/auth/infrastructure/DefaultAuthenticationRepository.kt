package com.lukasz.witkowski.training.planner.auth.infrastructure

import com.lukasz.witkowski.training.planner.auth.domain.AuthenticationRepository
import com.lukasz.witkowski.training.planner.auth.domain.model.AuthenticationResult
import com.lukasz.witkowski.training.planner.auth.domain.model.SignInForm
import com.lukasz.witkowski.training.planner.auth.domain.model.SignUpForm
import com.lukasz.witkowski.training.planner.auth.infrastructure.local.TokenStorage
import com.lukasz.witkowski.training.planner.auth.infrastructure.remote.AuthenticationRemoteDataSource
import com.lukasz.witkowski.training.planner.shared.utils.fold

class DefaultAuthenticationRepository(
    private val tokenStorage: TokenStorage,
    private val remoteDataSource: AuthenticationRemoteDataSource,
) : AuthenticationRepository {
    override suspend fun refreshAccessToken(): AuthenticationResult {
        TODO("Not yet implemented")
    }

    override suspend fun signUp(form: SignUpForm): AuthenticationResult {
        return remoteDataSource.signUp(form).fold(
            onSuccess = { tokens ->
                tokenStorage.saveTokens(tokens)
                AuthenticationResult.Success
            },
            onError = { failure ->
                AuthenticationResult.Failure(failure)
            },
        )
    }

    override suspend fun signIn(form: SignInForm): AuthenticationResult {
        return remoteDataSource.signIn(form).fold(
            onSuccess = { tokens ->
                tokenStorage.saveTokens(tokens)
                AuthenticationResult.Success
            },
            onError = { failure ->
                AuthenticationResult.Failure(failure)
            },
        )
    }
}
