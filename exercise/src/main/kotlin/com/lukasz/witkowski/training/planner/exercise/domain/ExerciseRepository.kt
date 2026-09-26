package com.lukasz.witkowski.training.planner.exercise.domain

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    suspend fun getById(id: ExerciseId): Exercise2

    fun queryExercises(query: ExerciseQuery): Flow<PagingData<Exercise2>>

    /**
     * Returns true when the insertion has finished
     */
    suspend fun insert(exercise: Exercise): Boolean

    suspend fun delete(exercise: Exercise): Boolean

    /**
     * Returns _true_ if the update was successful
     */
    suspend fun updateExercise(updatedExercise: Exercise): Boolean
}
