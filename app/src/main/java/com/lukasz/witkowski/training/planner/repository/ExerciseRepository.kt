package com.lukasz.witkowski.training.planner.repository

import com.lukasz.witkowski.training.planner.exercise.infrastructure.ExerciseDao
import com.lukasz.witkowski.shared.models.Category
import com.lukasz.witkowski.shared.models.Exercise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ExerciseRepository
constructor(
    private val exerciseDao: ExerciseDao
) {

    suspend fun insertExercise(exercise: Exercise): Long {
        return withContext(Dispatchers.IO) {
            exerciseDao.insert(exercise = exercise)
        }
    }

    fun loadExercises(filterCategories: List<Category> = emptyList()): Flow<List<Exercise>> {
        return if (filterCategories.isEmpty()) {
            exerciseDao.getAll()
        } else {
            exerciseDao.getExercisesFromCategories(filterCategories)
        }
    }

}