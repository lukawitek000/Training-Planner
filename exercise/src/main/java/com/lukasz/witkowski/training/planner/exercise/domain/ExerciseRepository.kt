package com.lukasz.witkowski.training.planner.exercise.domain

import kotlinx.coroutines.flow.Flow

// TODO does domain contains only interfaces to connect with infrastructure??
internal interface ExerciseRepository {
    fun getById(id: String): Flow<Exercise>
    fun getAll(): Flow<List<Exercise>>
    suspend fun insert(exercise: Exercise): Long

    // TODO is it better to delete by Id or object??
    suspend fun delete(exercise: Exercise)
}