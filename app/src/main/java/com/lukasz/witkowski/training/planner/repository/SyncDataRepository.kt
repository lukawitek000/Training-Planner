package com.lukasz.witkowski.training.planner.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.lukasz.witkowski.shared.db.TrainingDao
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import kotlinx.coroutines.flow.Flow

class SyncDataRepository
constructor( private val trainingDao: TrainingDao) {

    fun getNotSynchronizedTrainings() : LiveData<List<TrainingWithExercises>> {
        return trainingDao.getNotSynchronizedTrainingsWithExercises().asLiveData()
    }


}