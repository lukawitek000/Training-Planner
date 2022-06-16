package com.lukasz.witkowski.training.planner.statistics.application

import com.lukasz.witkowski.training.planner.statistics.domain.session.CircuitSetsPolicy
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSession
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.statistics.domain.session.statisticsrecorder.TimeProvider
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan

class TrainingSessionService(
    private val timeProvider: TimeProvider
) {

    private lateinit var trainingSession: TrainingSession

    fun startTraining(trainingPlan: TrainingPlan): TrainingSessionState {
        // TODO inject training strategy
        val trainingSetsStrategy = CircuitSetsPolicy()
        trainingSession = TrainingSession(trainingPlan, trainingSetsStrategy)
        return trainingSession.start(timeProvider.currentTime())
    }

    fun skip(): TrainingSessionState {
        return trainingSession.next(false, timeProvider.currentTime())
    }

    fun completed(): TrainingSessionState {
        return trainingSession.next(true, timeProvider.currentTime())
    }

    fun stopTraining() {
        trainingSession.stop()
    }
}
