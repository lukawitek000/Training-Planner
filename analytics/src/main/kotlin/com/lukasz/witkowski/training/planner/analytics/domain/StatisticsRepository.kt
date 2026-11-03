package com.lukasz.witkowski.training.planner.analytics.domain

import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlinx.coroutines.flow.Flow

interface StatisticsRepository {
    suspend fun save(trainingStatistics: TrainingStatistics)

    fun delete(trainingStatistics: TrainingStatistics)

    fun getByTrainingPlanId(trainingPlanId: TrainingPlanId): Flow<List<TrainingStatistics>>

    fun getByTrainingStatisticsId(
        trainingStatisticsId: TrainingStatisticsId
    ): Flow<TrainingStatistics>
}
