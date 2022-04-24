package com.lukasz.witkowski.training.planner.statistics.presentation

import com.lukasz.witkowski.training.planner.training.presentation.TrainingPlan
import kotlinx.coroutines.flow.StateFlow

interface TrainingSessionController {
    val trainingSessionState: StateFlow<TrainingSessionState>
    fun startTrainingSession(trainingPlan: TrainingPlan)
    fun skip()
    fun completed()
}
