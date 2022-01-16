package com.lukasz.witkowski.shared.repository

import com.lukasz.witkowski.shared.db.TrainingDao
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TrainingRepository constructor(
    private val trainingDao: TrainingDao
) {

    suspend fun fetchTrainingById(id: Long): TrainingWithExercises = withContext(Dispatchers.IO) {
        trainingDao.getTrainingWithExercisesByIdAsync(id)
    }
}