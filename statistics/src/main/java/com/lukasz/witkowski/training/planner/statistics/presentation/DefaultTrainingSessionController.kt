package com.lukasz.witkowski.training.planner.statistics.presentation

import com.lukasz.witkowski.training.planner.statistics.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingPlan
import com.lukasz.witkowski.training.planner.training.presentation.mappers.TrainingPlanMapper
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