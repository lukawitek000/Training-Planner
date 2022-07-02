package com.lukasz.witkowski.training.planner.exercise.application

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepository
import com.lukasz.witkowski.training.planner.exercise.domain.ImageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExerciseService(
    private val exerciseRepository: ExerciseRepository,
    private val imageRepository: ImageRepository
) {
    suspend fun saveExercise(exercise: Exercise): Boolean {
        return exerciseRepository.insert(exercise)
    }
    // TODO Should be all exercises taken from domain and then filter here, or the filtration should be made in infra (SQL query)? (less data transmission)
    fun getExercisesFromCategories(categories: List<ExerciseCategory>): Flow<List<Exercise>> {
        return exerciseRepository.getAll().map {
            it.filter { exercise -> categories.contains(exercise.category) || categories.isEmpty() }
        }
    }

    suspend fun deleteExercise(exercise: Exercise) {
        exerciseRepository.delete(exercise)
    }

    fun getExerciseById(id: ExerciseId): Flow<Exercise> {
        return exerciseRepository.getById(id)
    }

    suspend fun updateExercise(exercise: Exercise): Boolean {
        return exerciseRepository.updateExercise(exercise)
    }
}
