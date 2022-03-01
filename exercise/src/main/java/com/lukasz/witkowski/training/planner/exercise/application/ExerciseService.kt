package com.lukasz.witkowski.training.planner.exercise.application

import com.lukasz.witkowski.training.planner.exercise.domain.Category
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseService(
    private val exerciseRepository: ExerciseRepository
) {

    suspend fun createExercise(exercise: Exercise) : String {
        return exerciseRepository.insert(exercise)
    }

    fun getAllExercises() : Flow<List<Exercise>> {
        return exerciseRepository.getAll()
    }

    fun getAllExercises(categories: List<Category>) : Flow<List<Exercise>> {
        return exerciseRepository.getAll().map {
            it.filter { exercise ->  categories.contains(exercise.category) }
        }
    }

    suspend fun deleteExercise(exercise: Exercise) {
        exerciseRepository.delete(exercise)
    }

    fun getExerciseById(id: String) : Flow<Exercise> {
        return exerciseRepository.getById(id)
    }
}