package com.lukasz.witkowski.training.planner.statistics.application

import com.lukasz.witkowski.training.planner.statistics.domain.session.CircuitSetsPolicy
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSession
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSetsPolicy
import com.lukasz.witkowski.training.planner.statistics.domain.session.statisticsrecorder.TimeProvider
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TrainingSessionService(
    private val timeProvider: TimeProvider,
    private val trainingSetsStrategy: TrainingSetsPolicy = CircuitSetsPolicy()
) {

    private lateinit var trainingSession: TrainingSession

    private val _trainingSessionState =
        MutableStateFlow<TrainingSessionState>(TrainingSessionState.IdleState)
    val trainingSessionState: StateFlow<TrainingSessionState>
        get() = _trainingSessionState

    fun startTraining(trainingPlan: TrainingPlan) {
        trainingSession = TrainingSession(trainingPlan, trainingSetsStrategy)
        _trainingSessionState.value = trainingSession.start(timeProvider.currentTime())
    }

    fun skip() {
        _trainingSessionState.value = trainingSession.skip(timeProvider.currentTime())
    }

    fun completed() {
        _trainingSessionState.value = trainingSession.completed(timeProvider.currentTime())
    }

    fun stopTraining() {
        trainingSession.stop()
        _trainingSessionState.value = TrainingSessionState.IdleState
    }

    fun isTrainingSessionStarted() = trainingSessionState.value !is TrainingSessionState.IdleState
}
