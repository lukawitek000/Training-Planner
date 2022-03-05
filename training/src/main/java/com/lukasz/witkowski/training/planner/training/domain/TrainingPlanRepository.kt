package com.lukasz.witkowski.training.planner.training.domain

import kotlinx.coroutines.flow.Flow

interface TrainingPlanRepository {

    suspend fun save(trainingPlan: TrainingPlan): Long
    fun getAll(): Flow<List<TrainingPlan>>
    fun getById(id: String) : Flow<TrainingPlan>
    fun delete(trainingPlan: TrainingPlan)
}