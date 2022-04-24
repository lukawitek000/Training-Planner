package com.lukasz.witkowski.training.planner.statistics.application

import com.lukasz.witkowski.training.planner.statistics.domain.BasicStatisticsRecorder
import com.lukasz.witkowski.training.planner.statistics.domain.CircuitSetsStrategy
import com.lukasz.witkowski.training.planner.statistics.domain.SystemTimeProvider
import com.lukasz.witkowski.training.planner.statistics.domain.TrainingSession
import com.lukasz.witkowski.training.planner.statistics.domain.TrainingSessionState
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import kotlinx.coroutines.flow.MutableStateFlow


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
