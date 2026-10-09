package com.lukasz.witkowski.training.planner.network

import com.lukasz.witkowski.training.planner.shared.network.NetworkFailure

sealed interface TokenRefreshFailure {
    data object InvalidRefreshToken : TokenRefreshFailure
    data object UserNotFound : TokenRefreshFailure
    data class NetworkError(val networkFailure: NetworkFailure) : TokenRefreshFailure
    data object UnknownFailure : TokenRefreshFailure
}
