package com.lukasz.witkowski.training.planner.statistics.application

import com.lukasz.witkowski.training.planner.statistics.domain.session.CircuitSetsPolicy
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSession
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSetsPolicy
import com.lukasz.witkowski.training.planner.statistics.domain.session.statisticsrecorder.TimeProvider
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan

class TrainingSessionService(
    private val timeProvider: TimeProvider,
    private val trainingSetsStrategy: TrainingSetsPolicy = CircuitSetsPolicy()
) {

    private lateinit var trainingSession: TrainingSession

    fun startTraining(trainingPlan: TrainingPlan): TrainingSessionState {
        trainingSession = TrainingSession(trainingPlan, trainingSetsStrategy)
        return trainingSession.start(timeProvider.currentTime())
    }

    fun skip(): TrainingSessionState {
        return trainingSession.skip(timeProvider.currentTime())
    }

    fun completed(): TrainingSessionState {
        return trainingSession.completed(timeProvider.currentTime())
    }

    fun stopTraining() {
        trainingSession.stop()
    }
}
