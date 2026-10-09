package com.lukasz.witkowski.training.planner.auth.infrastructure.remote

import com.lukasz.witkowski.training.planner.auth.domain.model.AuthTokens
import com.lukasz.witkowski.training.planner.auth.domain.model.SignInForm
import com.lukasz.witkowski.training.planner.auth.domain.model.SignUpForm

interface AuthenticationRemoteDataSource {
    suspend fun signIn(signInForm: SignInForm): AuthTokens
    suspend fun signUp(signUpForm: SignUpForm): AuthTokens
}