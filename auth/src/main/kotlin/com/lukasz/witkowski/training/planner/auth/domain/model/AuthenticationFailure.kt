package com.lukasz.witkowski.training.planner.auth.domain.model

sealed interface AuthenticationFailure {
    data object UserNotFound : AuthenticationFailure
    data object InvalidRefreshToken: AuthenticationFailure
    data object UnknownFailure : AuthenticationFailure
}