package com.lukasz.witkowski.training.planner.repository

import com.lukasz.witkowski.shared.db.StatisticsDao
import com.lukasz.witkowski.shared.models.statistics.GeneralStatistics
import com.lukasz.witkowski.shared.models.statistics.TrainingCompleteStatistics
import com.lukasz.witkowski.shared.models.statistics.TrainingStatistics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.math.max

class StatisticsRepository constructor(private val statisticsDao: StatisticsDao) {

    fun getTrainingCompleteStatisticsByTrainingId(trainingId: Long): Flow<List<GeneralStatistics>> {
       return statisticsDao.getTrainingCompleteStatisticsByTrainingId(trainingId).map {
            it.map { trainingCompleteStatistics -> trainingCompleteStatistics.toGeneralStatistics() }
        }
//        val generalStatisticsList = mutableListOf<GeneralStatistics>()
//        val completeStatisticsList = statisticsDao.getTrainingCompleteStatisticsByTrainingId(trainingId = trainingId)
//        for(completeStatistics in completeStatisticsList) {
//            generalStatisticsList.add(
//                completeStatistics.toGeneralStatistics()
//            )
//        }
//        generalStatisticsList
    }

    private fun TrainingCompleteStatistics.toGeneralStatistics(): GeneralStatistics {
        val totalBurnedCalories = calculateTotalBurnedCalories(this)
        val maxHeartRate = calculateMaxHeartRate(this)
        return GeneralStatistics(
            statisticsId = trainingStatistics.id,
            time = trainingStatistics.totalTime,
            date = trainingStatistics.date,
            burnedCalories = totalBurnedCalories,
            maxHeartRate = maxHeartRate,
            heartRateDuringTraining = trainingStatistics.heartRateHistory
        )
    }

    private  fun calculateMaxHeartRate(completeStatistics: TrainingCompleteStatistics): Double {
        var maxHeartRate = 0.0
        completeStatistics.exercisesStatistics.forEach {
            maxHeartRate = max(it.heartRateStatistics.max, maxHeartRate)
        }
        return maxHeartRate
    }

    private fun calculateTotalBurnedCalories(completeStatistics: TrainingCompleteStatistics): Double {
        var totalBurnedCalories = 0.0
        completeStatistics.exercisesStatistics.forEach {
            totalBurnedCalories += it.burntCaloriesStatistics.burntCalories
        }
        return totalBurnedCalories
    }
}