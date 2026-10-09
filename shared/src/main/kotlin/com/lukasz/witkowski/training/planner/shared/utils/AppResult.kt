package com.lukasz.witkowski.training.planner.shared.utils

sealed interface AppResult<out T, out E> {
    data class Success<out T>(val value: T) : AppResult<T, Nothing>
    data class Error<out E>(val error: E) : AppResult<Nothing, E>
}

inline fun <T, E, R> AppResult<T, E>.fold(
    onSuccess: (T) -> R,
    onError: (E) -> R,
): R = when (this) {
    is AppResult.Success -> onSuccess(value)
    is AppResult.Error -> onError(error)
}

inline fun <T, E, R> AppResult<T, E>.map(transform: (T) -> R): AppResult<R, E> {
    return fold(
        onSuccess = { AppResult.Success(transform(it)) },
        onError = { AppResult.Error(it) },
    )
}

inline fun <T, E, R> AppResult<T, E>.mapError(transform: (E) -> R): AppResult<T, R> {
    return fold(
        onSuccess = { AppResult.Success(it) },
        onError = { AppResult.Error(transform(it)) },
    )
}
