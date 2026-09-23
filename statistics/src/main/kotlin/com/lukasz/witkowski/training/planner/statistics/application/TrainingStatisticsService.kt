package com.lukasz.witkowski.training.planner.statistics.application

import com.lukasz.witkowski.training.planner.statistics.domain.StatisticsRepository
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlinx.coroutines.flow.Flow

class TrainingStatisticsService(
    private val statisticsRepository: StatisticsRepository,
) {
    suspend fun save(trainingStatistics: TrainingStatistics) {
        statisticsRepository.save(trainingStatistics)
    }

    fun getStatistics(trainingPlanId: TrainingPlanId): Flow<List<TrainingStatistics>> =
        statisticsRepository.getByTrainingPlanId(trainingPlanId)

    fun getStatistics(trainingStatisticsId: TrainingStatisticsId): Flow<TrainingStatistics> =
        statisticsRepository.getByTrainingStatisticsId(trainingStatisticsId)
}
