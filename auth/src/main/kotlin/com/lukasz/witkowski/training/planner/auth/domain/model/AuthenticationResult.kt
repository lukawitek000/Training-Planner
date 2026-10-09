package com.lukasz.witkowski.training.planner.auth.domain.model

sealed interface AuthenticationResult {
    data object Success : AuthenticationResult
    data class Failure(val failure: AuthenticationFailure) : AuthenticationResult
}
