package com.lukasz.witkowski.training.planner.training.domain

import kotlinx.coroutines.flow.Flow

interface TrainingPlanSender {
    /**
     * Returns ids of successfully sent Training Plans
     */
    fun send(trainingPlans: List<TrainingPlan>): Flow<TrainingPlanId>
}