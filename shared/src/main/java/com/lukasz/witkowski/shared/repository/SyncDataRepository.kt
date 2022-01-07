package com.lukasz.witkowski.shared.repository


import com.lukasz.witkowski.shared.db.TrainingDao
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import kotlinx.coroutines.flow.Flow

class SyncDataRepository
constructor( private val trainingDao: TrainingDao) {

    fun getNotSynchronizedTrainings() : Flow<List<TrainingWithExercises>> {
        return trainingDao.getNotSynchronizedTrainingsWithExercises()
    }


}