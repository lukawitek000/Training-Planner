package com.lukasz.witkowski.training.planner.auth.domain.model

import com.lukasz.witkowski.training.planner.shared.network.NetworkFailure

sealed interface UserFailure {
    data object UserNotFound : UserFailure
    data object Unauthorized : UserFailure
    data class NetworkError(val networkFailure: NetworkFailure) : UserFailure
    data object UnknownFailure : UserFailure
}
