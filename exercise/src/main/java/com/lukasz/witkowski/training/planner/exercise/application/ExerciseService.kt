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

    // TODO Should be all exercises taken from domain and then filter here, or the filtration should be made in infra? (less data transmission)
    // Maybe it would be good to have separate methods in domain for getting exercises from all categories and for selected categories (Seems to me like business requirement)
    fun getExercisesFromCategories(categories: List<ExerciseCategory>): Flow<List<Exercise>> {
        return exerciseRepository.getAll().map {
            it.filter { exercise -> categories.contains(exercise.category) || categories.isEmpty() }
        }
    }

    suspend fun deleteExercise(exercise: Exercise) {
        exerciseRepository.delete(exercise)
    }

    fun getExerciseById(id: String): Flow<Exercise> {
        return exerciseRepository.getById(id)
    }
}
