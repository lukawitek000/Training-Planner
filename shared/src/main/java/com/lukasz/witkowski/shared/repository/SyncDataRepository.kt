package com.lukasz.witkowski.shared.repository


import com.lukasz.witkowski.shared.db.StatisticsDao
import com.lukasz.witkowski.shared.db.TrainingDao
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.shared.models.statistics.TrainingCompleteStatistics
import kotlinx.coroutines.flow.Flow

class SyncDataRepository
constructor(private val trainingDao: TrainingDao, private val statisticsDao: StatisticsDao) {

    fun getNotSynchronizedTrainings(): Flow<List<TrainingWithExercises>> {
        return trainingDao.getNotSynchronizedTrainingsWithExercises()
    }

    fun getNotSynchronizedStatistics(): Flow<List<TrainingCompleteStatistics>> {
        return statisticsDao.getNotSynchronizedStatistics()
    }
}