package com.lukasz.witkowski.training.planner.exercise.application

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepository
import kotlinx.coroutines.flow.Flow

class ExerciseService(
    private val exerciseRepository: ExerciseRepository
) {

    suspend fun createExercise(exercise: Exercise) {
        exerciseRepository.insert(exercise)
    }

    fun getAllExercises() : Flow<List<Exercise>> {
        return exerciseRepository.getAll()
    }

    suspend fun deleteExercise(exercise: Exercise) {
        exerciseRepository.delete(exercise)
    }

    fun getExerciseById(id: String) : Flow<Exercise> {
        return exerciseRepository.getById(id)
    }
}