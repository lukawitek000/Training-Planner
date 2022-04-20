package com.lukasz.witkowski.training.planner.statistics.domain

import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan

internal class TrainingSession(
    private val trainingPlan: TrainingPlan
) {

    private val id: TrainingSessionId = TrainingSessionId.create()

    private var currentSet = 1
    private val exercises: List<TrainingExercise>
        get() = trainingPlan.exercises

    private val currentSetExercises = mutableListOf<TrainingExercise>()

    init {
//        startRecordingTrainingStatistics(trainingPlan.id)
//        loadSet(currentSet)
//        loadExercise()
    }

    fun next(): TrainingSessionState {
        // TODO

        return TrainingSessionState.IdleState
    }
}