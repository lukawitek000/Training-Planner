package com.lukasz.witkowski.training.planner.exercise.application

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseService(
    private val exerciseRepository: ExerciseRepository
) {
    suspend fun saveExercise(exercise: Exercise): Boolean {
        return exerciseRepository.insert(exercise)
    }

    fun getAllExercises(): Flow<List<Exercise>> {
        return exerciseRepository.getAll()
    }

    fun getExercisesFromCategories(categories: List<ExerciseCategory>): Flow<List<Exercise>> {
        return exerciseRepository.getAll().map {
            it.filter { exercise -> categories.contains(exercise.category) }
        }
    }

    suspend fun deleteExercise(exercise: Exercise) {
        exerciseRepository.delete(exercise)
    }

    fun getExerciseById(id: String): Flow<Exercise> {
        return exerciseRepository.getById(id)
    }
}
