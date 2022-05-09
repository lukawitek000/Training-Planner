package com.lukasz.witkowski.training.planner.training.domain

import com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.SynchronizationStatus
import kotlinx.coroutines.flow.Flow

interface TrainingPlanSender {
    /**
     * Returns ids of successfully sent Training Plans
     */
    fun send(trainingPlans: List<TrainingPlan>): Flow<SynchronizationStatus>
}