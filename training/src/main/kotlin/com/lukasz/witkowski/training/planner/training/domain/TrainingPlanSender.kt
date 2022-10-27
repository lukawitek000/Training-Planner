package com.lukasz.witkowski.training.planner.training.domain

import com.lukasz.witkowski.training.planner.synchronization.SynchronizationStatus
import kotlinx.coroutines.flow.Flow

interface TrainingPlanSender {
    fun send(trainingPlans: List<TrainingPlan>): Flow<SynchronizationStatus<TrainingPlanId>>
}
