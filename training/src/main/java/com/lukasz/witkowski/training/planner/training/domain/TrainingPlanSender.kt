package com.lukasz.witkowski.training.planner.training.domain

import kotlinx.coroutines.flow.Flow

interface TrainingPlanSender {
    fun send(trainingPlans: List<TrainingPlan>): Flow<String>
}