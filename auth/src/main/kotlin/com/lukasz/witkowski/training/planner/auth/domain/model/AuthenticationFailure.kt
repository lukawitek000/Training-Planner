package com.lukasz.witkowski.training.planner.auth.domain.model

import com.lukasz.witkowski.training.planner.shared.network.NetworkFailure

sealed interface AuthenticationFailure {
    data object UserNotFound : AuthenticationFailure
    data object UserAlreadyExists : AuthenticationFailure
    data object IncorrectPassword : AuthenticationFailure
    data object InvalidRefreshToken : AuthenticationFailure
    data class NetworkError(val networkFailure: NetworkFailure) : AuthenticationFailure
    data object UnknownFailure : AuthenticationFailure
}
