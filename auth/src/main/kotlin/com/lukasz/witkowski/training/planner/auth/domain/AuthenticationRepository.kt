package com.lukasz.witkowski.training.planner.auth.domain

import com.lukasz.witkowski.training.planner.auth.domain.model.AuthenticationResult
import com.lukasz.witkowski.training.planner.auth.domain.model.SignInForm
import com.lukasz.witkowski.training.planner.auth.domain.model.SignUpForm

interface AuthenticationRepository {
    suspend fun signUp(form: SignUpForm): AuthenticationResult

    suspend fun signIn(form: SignInForm): AuthenticationResult

    suspend fun logOut(): AuthenticationResult
}
