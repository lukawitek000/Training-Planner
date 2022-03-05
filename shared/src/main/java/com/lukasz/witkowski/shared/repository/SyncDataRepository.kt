package com.lukasz.witkowski.shared.repository


import com.lukasz.witkowski.shared.db.StatisticsDao
import com.lukasz.witkowski.shared.models.statistics.TrainingCompleteStatistics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SyncDataRepository
constructor(private val statisticsDao: StatisticsDao) {


    fun getNotSynchronizedStatistics(): Flow<List<TrainingCompleteStatistics>> {
        return statisticsDao.getNotSynchronizedStatistics()
    }

    suspend fun insertTrainingCompleteStatistics(trainingCompleteStatistics: TrainingCompleteStatistics) =
        withContext(Dispatchers.IO) {
            trainingCompleteStatistics.trainingStatistics.isSynchronized = true
            statisticsDao.insertTrainingCompleteStatistics(trainingCompleteStatistics)
        }

    suspend fun updateSynchronizedStatistics(id: Long) = withContext(Dispatchers.IO) {
        statisticsDao.updateSynchronizedStatisticsById(id)
    }
}