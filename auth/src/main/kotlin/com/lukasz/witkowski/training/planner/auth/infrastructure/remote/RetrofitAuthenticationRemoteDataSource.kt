package com.lukasz.witkowski.training.planner.auth.infrastructure.remote

import com.lukasz.witkowski.training.planner.auth.domain.model.AuthTokens
import com.lukasz.witkowski.training.planner.auth.domain.model.SignInForm
import com.lukasz.witkowski.training.planner.auth.domain.model.SignUpForm
import com.lukasz.witkowski.training.planner.shared.time.TimeProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class RetrofitAuthenticationRemoteDataSource(
    private val api: AuthenticationApi,
    private val timeProvider: TimeProvider,
    private val ioDispatcher: CoroutineDispatcher,
) : AuthenticationRemoteDataSource {
    override suspend fun signIn(signInForm: SignInForm): AuthTokens = withContext(ioDispatcher) {
        api.login(
            signInForm.toLoginRequestDto()
        ).toAuthTokens(timeProvider.currentInstant())
    }

    override suspend fun signUp(signUpForm: SignUpForm): AuthTokens = withContext(ioDispatcher) {
        api.register(
            signUpForm.toRegisterRequestDto()
        ).toAuthTokens(timeProvider.currentInstant())
    }
}
