package com.lukasz.witkowski.training.planner.exercise.domain

import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseConflictDto
import com.lukasz.witkowski.training.planner.shared.network.NetworkFailure

sealed interface ExerciseFailure {
    data object ExerciseNotFound : ExerciseFailure

    data object Unauthorized : ExerciseFailure

    data object Forbidden : ExerciseFailure

    data class Conflict(val conflictDto: ExerciseConflictDto? = null) : ExerciseFailure

    data class NetworkError(val networkFailure: NetworkFailure) : ExerciseFailure

    data object UnknownFailure : ExerciseFailure
}

fun ExerciseFailure.isRetryable(): Boolean = when (this) {
    is ExerciseFailure.NetworkError -> when (val nf = networkFailure) {
        is NetworkFailure.NoInternet,
        is NetworkFailure.Timeout,
        is NetworkFailure.Unknown -> true
        is NetworkFailure.HttpError -> nf.statusCode >= 500 || nf.statusCode == 429
        is NetworkFailure.SerializationError -> false
    }
    ExerciseFailure.Unauthorized -> true
    is ExerciseFailure.Conflict,
    ExerciseFailure.Forbidden,
    ExerciseFailure.ExerciseNotFound,
    ExerciseFailure.UnknownFailure -> false
}
