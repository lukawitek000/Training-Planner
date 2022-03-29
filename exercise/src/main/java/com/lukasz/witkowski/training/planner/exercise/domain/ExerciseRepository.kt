package com.lukasz.witkowski.training.planner.exercise.domain

import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun getById(id: String): Flow<Exercise>
    fun getAll(): Flow<List<Exercise>>
    /**
     * Returns true when the insertion has finished
     */
    suspend fun insert(exercise: Exercise): Boolean

    // TODO is it better to delete by Id or object??
    suspend fun delete(exercise: Exercise)
}