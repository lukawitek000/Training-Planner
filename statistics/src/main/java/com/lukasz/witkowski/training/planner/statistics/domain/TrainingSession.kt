package com.lukasz.witkowski.training.planner.statistics.domain

import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan

/**
 * Training session aggregate - controls state of the training session.
 * Requires [TrainingPlan] with at least one [TrainingExercise].
 */
internal class TrainingSession(
    private val trainingPlan: TrainingPlan,
    private val statisticsRecorder: StatisticsRecorder
) {

    //    private var currentSet = 1 // For statistics purposes
    private val exercises = mutableListOf<TrainingExercise>()
    private var state: TrainingSessionState = TrainingSessionState.IdleState
    private val currentExercise: TrainingExercise
        get() = state.exercise!!

    init {
        require(trainingPlan.exercises.isNotEmpty()) { "Cannot start training session without exercises" }
        loadExercisesFromTrainingPlan() // thanks to that I can inject later different strategies for exercises order
    }

    fun start(): TrainingSessionState {
        statisticsRecorder.start()
        val firstExercise = loadExercise()
        statisticsRecorder.startRecordingExercise(firstExercise.id, 1)
        state = TrainingSessionState.ExerciseState(firstExercise)
        return state
    }

    fun next(): TrainingSessionState {
        statisticsRecorder.stopRecordingExercise()
        state = when {
            isTrainingSessionFinished() -> TrainingSessionState.SummaryState(statisticsRecorder.trainingStatistics)
            isExerciseState() && hasCurrentExerciseRestTime() -> {
                val nextExercise = getNextExerciseOverview()
                TrainingSessionState.RestTimeState(nextExercise, currentExercise.restTime)
            }
            isRestTimeState() || (isExerciseState() && !hasCurrentExerciseRestTime()) -> {
                val currentExercise = loadExercise()
                TrainingSessionState.ExerciseState(currentExercise)
            }
            else -> throw Exception("Unknown training session state")
        }
        return state
    }

    private fun loadExercisesFromTrainingPlan() {
        val maxSets = trainingPlan.exercises.maxOf { it.sets }
        for (set in 1..maxSets) {
            val setExercises = trainingPlan.exercises.filter { it.sets >= set }
            exercises.addAll(setExercises)
        }
    }

    private fun getNextExerciseOverview() = exercises.first()

    private fun loadExercise(): TrainingExercise = exercises.removeFirst()

    private fun isTrainingSessionFinished(): Boolean = exercises.isEmpty()

    private fun isRestTimeState() = state is TrainingSessionState.RestTimeState

    private fun isExerciseState() = state is TrainingSessionState.ExerciseState

    private fun hasCurrentExerciseRestTime() =
        (state as? TrainingSessionState.ExerciseState)?.exercise?.restTime?.isNotZero() == true
}