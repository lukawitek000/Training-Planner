package com.lukasz.witkowski.training.planner.training.domain

import kotlinx.coroutines.flow.Flow

interface TrainingPlanRepository {
    suspend fun save(trainingPlan: TrainingPlan): Result<TrainingPlanId>

    fun getAll(): Flow<List<TrainingPlanOverview>>

    suspend fun delete(trainingPlanId: TrainingPlanId): Result<Unit>

    suspend fun getTrainingPlanById(trainingPlanId: TrainingPlanId): Flow<TrainingPlan>

    suspend fun update(trainingPlan: TrainingPlan) : Result<TrainingPlanId>
}
