package com.lukasz.witkowski.training.planner.repository

import com.lukasz.witkowski.shared.db.StatisticsDao
import com.lukasz.witkowski.shared.models.statistics.TrainingCompleteStatistics
import com.lukasz.witkowski.shared.models.statistics.TrainingStatistics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StatisticsRepository constructor(private val statisticsDao: StatisticsDao) {

    suspend fun getTrainingCompleteStatisticsByTrainingId(trainingId: Long): TrainingCompleteStatistics =
        withContext(Dispatchers.IO) {
            statisticsDao.getTrainingCompleteStatisticsByTrainingId(trainingId = trainingId)
        }
}