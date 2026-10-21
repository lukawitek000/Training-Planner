package com.lukasz.witkowski.training.planner.exercise.infrastructure.remote

import com.lukasz.witkowski.training.planner.dto.common.PagedResponseDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseSyncResponseDto
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise2
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseFailure
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.shared.utils.AppResult

interface ExerciseRemoteDataSource {
    suspend fun getCategories(): AppResult<List<ExerciseCategory>, ExerciseFailure>

    suspend fun getExerciseChanges(
        sinceVersion: Long
    ): AppResult<ExerciseSyncResponseDto, ExerciseFailure>

    suspend fun getExercises(
        query: String? = null,
        categoryId: String? = null,
        page: Int? = null,
        limit: Int? = null,
        currentUserId: String? = null
    ): AppResult<PagedResponseDto<Exercise2>, ExerciseFailure>

    suspend fun getExerciseById(
        id: ExerciseId,
        currentUserId: String? = null
    ): AppResult<Exercise2, ExerciseFailure>

    suspend fun getExerciseDetailsById(
        id: ExerciseId,
        currentUserId: String? = null
    ): AppResult<ExerciseDetails, ExerciseFailure>

    suspend fun createExercise(exercise: Exercise2): AppResult<Exercise2, ExerciseFailure>

    suspend fun createExercise(
        exerciseDetails: ExerciseDetails,
        idempotencyKey: String? = null
    ): AppResult<ExerciseDto, ExerciseFailure>

    suspend fun updateExercise(
        exerciseDetails: ExerciseDetails,
        expectedVersion: Long? = null,
        idempotencyKey: String? = null
    ): AppResult<ExerciseDto, ExerciseFailure>

    suspend fun deleteExercise(id: ExerciseId): AppResult<Unit, ExerciseFailure>
}
