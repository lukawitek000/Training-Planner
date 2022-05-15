package com.lukasz.witkowski.training.planner.statistics.application

import com.lukasz.witkowski.training.planner.statistics.domain.session.statisticsrecorder.BasicStatisticsRecorder
import com.lukasz.witkowski.training.planner.statistics.domain.session.CircuitSetsStrategy
import com.lukasz.witkowski.training.planner.statistics.domain.session.statisticsrecorder.SystemTimeProvider
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSession
import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan


class TrainingSessionService {

    private lateinit var trainingSession: TrainingSession

    fun startTraining(trainingPlan: TrainingPlan): TrainingSessionState {
        val timeProvider = SystemTimeProvider()
        val statisticsRecorder = BasicStatisticsRecorder(trainingPlan.id, timeProvider)
        val trainingSetsStrategy = CircuitSetsStrategy()
        trainingSession = TrainingSession(trainingPlan, statisticsRecorder, trainingSetsStrategy)
        return trainingSession.start()
    }

    fun skip(): TrainingSessionState {
        return trainingSession.next(false)
    }

    fun completed(): TrainingSessionState {
        return trainingSession.next(true)
    }

    fun stopTraining() {
        trainingSession.stop()
    }
}
