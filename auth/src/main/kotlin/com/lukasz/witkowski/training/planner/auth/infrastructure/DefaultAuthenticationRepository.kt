package com.lukasz.witkowski.training.planner.auth.infrastructure

import com.lukasz.witkowski.training.planner.auth.domain.AuthenticationRepository
import com.lukasz.witkowski.training.planner.auth.infrastructure.local.TokenStorage
import com.lukasz.witkowski.training.planner.auth.domain.model.AuthenticationResult
import com.lukasz.witkowski.training.planner.auth.domain.model.SignInForm
import com.lukasz.witkowski.training.planner.auth.domain.model.SignUpForm
import com.lukasz.witkowski.training.planner.auth.infrastructure.remote.AuthenticationRemoteDataSource
import com.lukasz.witkowski.training.planner.shared.utils.runCatchingCancellable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class DefaultAuthenticationRepository(
    private val tokenStorage: TokenStorage,
    private val remoteDataSource: AuthenticationRemoteDataSource,
) : AuthenticationRepository {
    override suspend fun refreshAccessToken(): AuthenticationResult {
        TODO("Not yet implemented")
    }

    override suspend fun signUp(form: SignUpForm): AuthenticationResult {
        val authTokens = remoteDataSource.signUp(form)
        tokenStorage.saveTokens(authTokens)
        // TODO add handling failures, need to extend backend for more meaningful ones
        return AuthenticationResult.Success
    }

    override suspend fun signIn(form: SignInForm): AuthenticationResult {
        val authTokens = remoteDataSource.signIn(form)
        tokenStorage.saveTokens(authTokens)
        return AuthenticationResult.Success
    }
}