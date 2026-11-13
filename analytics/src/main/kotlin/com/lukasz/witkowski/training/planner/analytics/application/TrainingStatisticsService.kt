package com.lukasz.witkowski.training.planner.analytics.application

import androidx.paging.PagingData
import com.lukasz.witkowski.training.planner.analytics.domain.StatisticsRepository
import com.lukasz.witkowski.training.planner.analytics.domain.models.TrainingStatisticsOverview
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlinx.coroutines.flow.Flow

class TrainingStatisticsService(private val statisticsRepository: StatisticsRepository) {
    suspend fun save(trainingStatistics: TrainingStatistics) {
        statisticsRepository.save(trainingStatistics)
    }

    fun getStatistics(trainingPlanId: TrainingPlanId): Flow<List<TrainingStatistics>> =
        statisticsRepository.getByTrainingPlanId(trainingPlanId)

    fun getOverviews(trainingPlanId: TrainingPlanId): Flow<PagingData<TrainingStatisticsOverview>> =
        statisticsRepository.getOverviewsByTrainingPlanId(trainingPlanId)

    fun getStatistics(trainingStatisticsId: TrainingStatisticsId): Flow<TrainingStatistics?> =
        statisticsRepository.getByTrainingStatisticsId(trainingStatisticsId)
}
