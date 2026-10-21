package com.lukasz.witkowski.training.planner.exercise.domain

import com.lukasz.witkowski.training.planner.shared.utils.AppResult

interface ExerciseSyncRepository {
    suspend fun synchronizeExercises(): AppResult<Unit, ExerciseFailure>

    suspend fun processPendingMutations(): AppResult<Unit, ExerciseFailure>

    suspend fun getConflictServerExercise(
        exerciseId: ExerciseId
    ): AppResult<ExerciseDetails, ExerciseFailure>

    suspend fun resolveConflictKeepLocal(exerciseId: ExerciseId): AppResult<Unit, ExerciseFailure>

    suspend fun resolveConflictAcceptServer(
        exerciseId: ExerciseId
    ): AppResult<Unit, ExerciseFailure>

    suspend fun onUserLogout(keepLocalData: Boolean = false)
}
