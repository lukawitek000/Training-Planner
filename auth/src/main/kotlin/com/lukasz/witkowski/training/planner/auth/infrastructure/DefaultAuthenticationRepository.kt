package com.lukasz.witkowski.training.planner.auth.infrastructure

import com.lukasz.witkowski.training.planner.auth.domain.AuthenticationRepository
import com.lukasz.witkowski.training.planner.auth.domain.model.AuthenticationFailure
import com.lukasz.witkowski.training.planner.auth.domain.model.AuthenticationResult
import com.lukasz.witkowski.training.planner.auth.domain.model.SignInForm
import com.lukasz.witkowski.training.planner.auth.domain.model.SignUpForm
import com.lukasz.witkowski.training.planner.auth.infrastructure.remote.AuthenticationRemoteDataSource
import com.lukasz.witkowski.training.planner.network.AuthTokens
import com.lukasz.witkowski.training.planner.network.TokenStorage
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import com.lukasz.witkowski.training.planner.shared.utils.fold
import com.lukasz.witkowski.training.planner.shared.utils.runCatchingCancellable
import timber.log.Timber

class DefaultAuthenticationRepository(
    private val tokenStorage: TokenStorage,
    private val remoteDataSource: AuthenticationRemoteDataSource,
) : AuthenticationRepository {

    override suspend fun signUp(form: SignUpForm): AuthenticationResult {
        Timber.i("Repository signUp called for email: %s, username: %s", form.email, form.username)
        return remoteDataSource.signUp(form).processResult()
    }

    override suspend fun signIn(form: SignInForm): AuthenticationResult {
        Timber.i("Repository signIn called for email: %s", form.email)
        return remoteDataSource.signIn(form).processResult()
    }

    override suspend fun logOut(): AuthenticationResult {
        Timber.i("Repository logOut called - clearing local token storage")
        return runCatchingCancellable {
            tokenStorage.clear()
        }.fold(
            onSuccess = {
                Timber.i("LogOut successful - tokens cleared")
                AuthenticationResult.Success
            },
            onFailure = { error ->
                Timber.e(error, "LogOut failed during token clearing")
                AuthenticationResult.Failure(AuthenticationFailure.UnknownFailure)
            },
        )
    }

    private suspend fun AppResult<AuthTokens, AuthenticationFailure>.processResult(): AuthenticationResult =
        fold(
            onSuccess = { tokens ->
                Timber.i("Authentication remote call succeeded - saving tokens")
                tokenStorage.saveTokens(tokens)
                AuthenticationResult.Success
            },
            onError = { failure ->
                Timber.w("Authentication remote call returned failure: %s", failure)
                AuthenticationResult.Failure(failure)
            },
        )
}
