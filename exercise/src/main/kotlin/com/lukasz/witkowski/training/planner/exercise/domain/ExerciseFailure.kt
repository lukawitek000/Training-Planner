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
