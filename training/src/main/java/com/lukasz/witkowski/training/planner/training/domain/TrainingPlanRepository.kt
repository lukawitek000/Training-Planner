package com.lukasz.witkowski.training.planner.training.domain

import kotlinx.coroutines.flow.Flow

interface TrainingPlanRepository {

    suspend fun save(trainingPlan: TrainingPlan)
    fun getAll(): Flow<List<TrainingPlan>>
    suspend fun delete(trainingPlan: TrainingPlan)
}