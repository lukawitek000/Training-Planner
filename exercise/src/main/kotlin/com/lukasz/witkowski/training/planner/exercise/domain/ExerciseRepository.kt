package com.lukasz.witkowski.training.planner.exercise.domain

import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    suspend fun getById(id: ExerciseId): Exercise
    fun getAll(): Flow<List<Exercise>>
    /**
     * Returns true when the insertion has finished
     */
    suspend fun insert(exercise: Exercise): Boolean

    suspend fun delete(exercise: Exercise)

    /**
     * Returns _true_ if the update was successful
     */
    suspend fun updateExercise(updatedExercise: Exercise): Boolean
}
