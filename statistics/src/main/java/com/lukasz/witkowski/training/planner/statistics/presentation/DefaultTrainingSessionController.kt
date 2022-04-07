package com.lukasz.witkowski.training.planner.statistics.presentation

import com.lukasz.witkowski.training.planner.statistics.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.training.presentation.TrainingPlan
import com.lukasz.witkowski.training.planner.training.presentation.TrainingPlanMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// TODO where to do controlling training progress??
// Calculating which training exercises is now or if it is rest time or summary
class DefaultTrainingSessionController(
    private val trainingSessionService: TrainingSessionService
) : TrainingSessionController {

    private val _trainingSessionState =
        MutableStateFlow<TrainingSessionState>(TrainingSessionState.RestTimeState(16000L))
    override val trainingSessionState: StateFlow<TrainingSessionState>
        get() = _trainingSessionState

    override fun startTrainingSession(trainingPlan: TrainingPlan) {
        val domainTrainingPlan = TrainingPlanMapper.toDomainTrainingPlan(trainingPlan)
        trainingSessionService.startTraining(domainTrainingPlan)
    }

    override fun skip() {
        TODO("Not yet implemented")
    }

    override fun completed() {
        TODO("Not yet implemented")
    }
}