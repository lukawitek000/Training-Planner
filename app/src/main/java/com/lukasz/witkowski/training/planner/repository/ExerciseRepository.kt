package com.lukasz.witkowski.training.planner.repository

import androidx.lifecycle.LiveData
import com.lukasz.witkowski.shared.db.ExerciseDao
import com.lukasz.witkowski.shared.models.Exercise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExerciseRepository
constructor(
    private val exerciseDao: ExerciseDao
){

    suspend fun insertExercise(exercise: Exercise) : Long {
        return withContext(Dispatchers.IO) {
            exerciseDao.insert(exercise = exercise)
        }
    }

    fun loadAllExercises(): LiveData<List<Exercise>> {
        return exerciseDao.getAll()
    }
}