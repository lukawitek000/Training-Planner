package com.lukasz.witkowski.training.planner.exercise.application

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

//Adrian: Good moment to write some tests.
class ExerciseService(
    private val exerciseRepository: ExerciseRepository
) {

    suspend fun saveExercise(exercise: Exercise): Boolean {
        return exerciseRepository.insert(exercise)
    }
    // TODO Should be all exercises taken from domain and then filter here, or the filtration should be made in infra (SQL query)? (less data transmission)
    //Adrian: I'm not sure if it is needed to have Flow/Coroutines here. Everything is handled on the single thread
    //so I bet it can be handled in ViewModel
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
