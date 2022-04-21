package com.lukasz.witkowski.training.planner.statistics.application

import com.lukasz.witkowski.training.planner.statistics.domain.BasicStatisticsRecorder
import com.lukasz.witkowski.training.planner.statistics.domain.CircuitSetsStrategy
import com.lukasz.witkowski.training.planner.statistics.domain.TrainingSession
import com.lukasz.witkowski.training.planner.statistics.domain.TrainingSessionState
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import kotlinx.coroutines.flow.MutableStateFlow


class TrainingSessionService {

    private lateinit var trainingSession: TrainingSession
    val trainingSessionState =
        MutableStateFlow<TrainingSessionState>(TrainingSessionState.IdleState)

    fun startTraining(trainingPlan: TrainingPlan) {
        val statisticsRecorder = BasicStatisticsRecorder(trainingPlan.id)
        val trainingSetsStrategy = CircuitSetsStrategy()
        trainingSession = TrainingSession(trainingPlan, statisticsRecorder, trainingSetsStrategy)
        trainingSessionState.value = trainingSession.start()
    }

    fun skip() {
        trainingSessionState.value = trainingSession.next(false)
    }

    fun completed() {
        trainingSessionState.value = trainingSession.next(true)
    }

    fun stopTraining() {
        trainingSession.stop()
    }
}
