package com.lukasz.witkowski.training.planner.auth.infrastructure.remote

import com.lukasz.witkowski.training.planner.auth.domain.model.AuthTokens
import com.lukasz.witkowski.training.planner.auth.domain.model.AuthenticationFailure
import com.lukasz.witkowski.training.planner.auth.domain.model.SignInForm
import com.lukasz.witkowski.training.planner.auth.domain.model.SignUpForm
import com.lukasz.witkowski.training.planner.shared.utils.AppResult

interface AuthenticationRemoteDataSource {
    suspend fun signIn(signInForm: SignInForm): AppResult<AuthTokens, AuthenticationFailure>
    suspend fun signUp(signUpForm: SignUpForm): AppResult<AuthTokens, AuthenticationFailure>
}
