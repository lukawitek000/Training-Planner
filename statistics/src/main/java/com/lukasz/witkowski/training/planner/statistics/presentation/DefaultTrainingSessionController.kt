package com.lukasz.witkowski.training.planner.statistics.presentation

import com.lukasz.witkowski.training.planner.statistics.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.training.presentation.TrainingExercise
import com.lukasz.witkowski.training.planner.training.presentation.TrainingExerciseMapper
import com.lukasz.witkowski.training.planner.training.presentation.TrainingPlan
import com.lukasz.witkowski.training.planner.training.presentation.TrainingPlanMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DefaultTrainingSessionController(
    private val trainingSessionService: TrainingSessionService
) : TrainingSessionController {

    private val _trainingSessionState =
        MutableStateFlow<TrainingSessionState>(TrainingSessionState.IdleState)
    override val trainingSessionState: StateFlow<TrainingSessionState>
        get() = _trainingSessionState

    override fun startTrainingSession(trainingPlan: TrainingPlan) {
        TODO("Not yet implemented")
    }

    override fun skip() {
        TODO("Not yet implemented")
    }

    override fun completed() {
        TODO("Not yet implemented")
    }
}