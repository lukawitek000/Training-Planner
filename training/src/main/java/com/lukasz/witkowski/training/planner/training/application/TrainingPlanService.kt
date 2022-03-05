package com.lukasz.witkowski.training.planner.training.application

import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanRepository
import kotlinx.coroutines.flow.Flow

class TrainingPlanService(
    private val trainingPlanRepository: TrainingPlanRepository
){

    suspend fun saveTrainingPlan(trainingPlan: TrainingPlan) {
        trainingPlanRepository.save(trainingPlan)
    }

    fun getAllTrainingPlans(): Flow<List<TrainingPlan>> {
        return trainingPlanRepository.getAll()
    }

}