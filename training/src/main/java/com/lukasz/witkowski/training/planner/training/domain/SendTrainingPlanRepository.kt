package com.lukasz.witkowski.training.planner.training.domain

import kotlinx.coroutines.flow.Flow

// TODO how to name this repository??
interface SendTrainingPlanRepository {
    fun send(trainingPlans: List<TrainingPlan>): Flow<String>
}