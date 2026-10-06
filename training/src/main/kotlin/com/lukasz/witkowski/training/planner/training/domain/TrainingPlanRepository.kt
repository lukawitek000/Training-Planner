package com.lukasz.witkowski.training.planner.training.domain

import kotlinx.coroutines.flow.Flow

interface TrainingPlanRepository {
    suspend fun save(
        trainingPlanConfiguration: TrainingPlanConfiguration,
        id: TrainingPlanId
    ): Result<TrainingPlanId>

    fun getAll(trainingQuery: TrainingQuery): Flow<List<TrainingPlanOverview>>

    suspend fun delete(trainingPlanId: TrainingPlanId): Result<Unit>

    suspend fun getTrainingPlanById(trainingPlanId: TrainingPlanId): Flow<TrainingPlan?>

    suspend fun update(
        trainingPlanConfiguration: TrainingPlanConfiguration,
        id: TrainingPlanId
    ): Result<TrainingPlanId>

    suspend fun useTrainingPlan(trainingPlanId: TrainingPlanId): Result<TrainingPlan>
}
