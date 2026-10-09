package com.lukasz.witkowski.training.planner.shared.network

sealed interface NetworkFailure {
    data object NoInternet : NetworkFailure
    data object Timeout : NetworkFailure
    data class HttpError(
        val statusCode: Int,
        val message: String?,
        val errorCode: String? = null,
    ) : NetworkFailure

    data class SerializationError(val message: String?) : NetworkFailure
    data object Unknown : NetworkFailure
}
