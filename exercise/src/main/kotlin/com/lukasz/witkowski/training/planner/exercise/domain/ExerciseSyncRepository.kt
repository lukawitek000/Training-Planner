package com.lukasz.witkowski.training.planner.exercise.domain

import com.lukasz.witkowski.training.planner.shared.utils.AppResult

interface ExerciseSyncRepository {
    suspend fun synchronizeExercises(): AppResult<Unit, ExerciseFailure>
}
