package com.lukasz.witkowski.training.planner.training.domain

import kotlinx.coroutines.flow.Flow

// TrainingPlanSendER  Connector, calculator, transformer (It is not a repository, it doesn't persist data)
interface TrainingPlanSender {
    fun send(trainingPlans: List<TrainingPlan>): Flow<String>
}