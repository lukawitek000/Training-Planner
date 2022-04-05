package com.lukasz.witkowski.training.planner.training.domain

import kotlinx.coroutines.flow.Flow

// Repository = read update delete - keeping state, saving data
interface TrainingPlanRepository {

    suspend fun save(trainingPlan: TrainingPlan)
    suspend fun setTrainingPlanAsSynchronized(id: String)
    fun getAll(): Flow<List<TrainingPlan>>
    suspend fun delete(trainingPlan: TrainingPlan)
    fun getTrainingPlanById(trainingPlanId: TrainingPlanId): Flow<TrainingPlan>
}