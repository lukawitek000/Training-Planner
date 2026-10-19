package com.lukasz.witkowski.training.planner.exercise.domain

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun getExerciseDetailsById(id: ExerciseId): Flow<ExerciseDetails?>

    fun queryExercises(query: ExerciseQuery): Flow<PagingData<Exercise2>>

    /**
     * Returns true when the insertion has finished
     */
    suspend fun insert(exercise: ExerciseDetails): Boolean

    suspend fun delete(exerciseId: ExerciseId): Boolean

    /**
     * Returns _true_ if the update was successful
     */
    suspend fun updateExercise(exercise: ExerciseDetails): Boolean

    /**
     * Deliberately saves a remote exercise to local storage. Returns _true_ if successful.
     */
    suspend fun saveExerciseLocally(exerciseDetails: ExerciseDetails): Boolean

    suspend fun resolveConflictKeepLocal(exerciseId: ExerciseId, latestServerVersion: Long): Boolean

    suspend fun resolveConflictAcceptServer(exerciseId: ExerciseId, serverExerciseDetails: ExerciseDetails): Boolean
}
