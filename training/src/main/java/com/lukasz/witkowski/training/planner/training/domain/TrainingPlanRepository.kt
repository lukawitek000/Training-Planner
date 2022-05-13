package com.lukasz.witkowski.training.planner.training.domain

import kotlinx.coroutines.flow.Flow

// Repository = read update delete - keeping state, saving data
interface TrainingPlanRepository {

    suspend fun save(trainingPlan: TrainingPlan)
    suspend fun setTrainingPlanAsSynchronized(id: TrainingPlanId)
    fun getAll(): Flow<List<TrainingPlan>>
    suspend fun delete(trainingPlan: TrainingPlan)
    suspend fun getTrainingPlanById(trainingPlanId: TrainingPlanId): TrainingPlan
}