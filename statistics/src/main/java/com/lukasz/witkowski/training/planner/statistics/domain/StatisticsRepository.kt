package com.lukasz.witkowski.training.planner.statistics.domain

import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlinx.coroutines.flow.Flow

interface StatisticsRepository {
    suspend fun save(trainingStatistics: TrainingStatistics)
    fun delete(trainingStatistics: TrainingStatistics)
    fun getByTrainingPlanId(trainingPlanId: TrainingPlanId): Flow<List<TrainingStatistics>>
}
