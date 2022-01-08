package com.lukasz.witkowski.training.planner.repository

import com.lukasz.witkowski.shared.db.TrainingDao
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TrainingRepository constructor(
    private val trainingDao: TrainingDao
) {

    fun getAllTrainings() = trainingDao.getAllTrainings()

    suspend fun insertTrainingWithExercises(trainingWithExercises: TrainingWithExercises) {
        withContext(Dispatchers.IO) {
            trainingDao.insertTrainingWithTrainingExercises(trainingWithExercises = trainingWithExercises)
        }
    }
}