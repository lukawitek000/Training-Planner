package com.lukasz.witkowski.training.planner.exercise.domain

import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun getById(id: ExerciseId): Flow<Exercise>
    fun getAll(): Flow<List<Exercise>>
    /**
     * Returns true when the insertion has finished
     */
    suspend fun insert(exercise: Exercise): Boolean

    suspend fun delete(exercise: Exercise)
}
