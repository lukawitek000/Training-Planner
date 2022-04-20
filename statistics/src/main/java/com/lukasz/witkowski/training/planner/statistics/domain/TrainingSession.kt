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

    fun start(): TrainingSessionState {
        loadSet(currentSet)
        val firstExercise = loadExercise()
        return TrainingSessionState.ExerciseState(firstExercise)
    }

    fun next(): TrainingSessionState {
        // TODO

        return TrainingSessionState.IdleState
    }

    private fun loadSet(setNumber: Int) {
        currentSetExercises.clear()
        val nextSetExercises = exercises.filter { it.sets >= setNumber }
        currentSetExercises.addAll(nextSetExercises)
    }

    private fun loadExercise(): TrainingExercise {
        return if (currentSetExercises.isNotEmpty()) {
            currentSetExercises.removeFirst()
        } else {
            loadSet(++currentSet)
            loadExercise()
        }
    }
}