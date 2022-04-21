package com.lukasz.witkowski.training.planner.statistics.domain

import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan

/**
 * Training session aggregate - controls state of the training session.
 * Requires [TrainingPlan] with at least one [TrainingExercise].
 */
internal class TrainingSession(
    private val trainingPlan: TrainingPlan,
    private val statisticsRecorder: StatisticsRecorder,
    private val trainingSetsStrategy: TrainingSetsStrategy
) {

    private val exercises = mutableListOf<TrainingExercise>()
    private var state: TrainingSessionState = TrainingSessionState.IdleState
    private val currentExercise: TrainingExercise
        get() = state.exercise!!

    init {
        require(trainingPlan.exercises.isNotEmpty()) { "Cannot start training session without exercises" }
        exercises.addAll(trainingSetsStrategy.loadExercises(trainingPlan)) // thanks to that I can inject later different strategies for exercises order
    }

    fun start(): TrainingSessionState {
        statisticsRecorder.start()
        val firstExercise = loadExercise()
        statisticsRecorder.startRecordingExercise(firstExercise.id, 1)
        state = TrainingSessionState.ExerciseState(firstExercise)
        return state
    }

    fun next(isCompleted: Boolean = false): TrainingSessionState {
        statisticsRecorder.stopRecordingExercise(isCompleted)
        state = when {
            isTrainingSessionFinished() -> TrainingSessionState.SummaryState(statisticsRecorder.trainingStatistics)
            isExerciseState() && hasCurrentExerciseRestTime() -> {
                val nextExercise = getNextExerciseOverview()
                TrainingSessionState.RestTimeState(nextExercise, currentExercise.restTime)
            }
            isRestTimeState() || (isExerciseState() && !hasCurrentExerciseRestTime()) -> {
                val currentExercise = loadExercise()
                statisticsRecorder.startRecordingExercise(currentExercise.id, getCurrentSet(currentExercise))
                TrainingSessionState.ExerciseState(currentExercise)
            }
            else -> throw Exception("Unknown training session state")
        }
        return state
    }

    fun stop() {
        exercises.clear()
        state = TrainingSessionState.IdleState
    }

    private fun getNextExerciseOverview() = exercises.first()

    private fun loadExercise(): TrainingExercise = exercises.removeFirst()

    private fun getCurrentSet(currentExercise: TrainingExercise): Int {
        val numberOfLeftAttempts = exercises.count { it.id == currentExercise.id }
        return currentExercise.sets - numberOfLeftAttempts
    }

    private fun isTrainingSessionFinished(): Boolean = exercises.isEmpty()

    private fun isRestTimeState() = state is TrainingSessionState.RestTimeState

    private fun isExerciseState() = state is TrainingSessionState.ExerciseState

    private fun hasCurrentExerciseRestTime() =
        (state as? TrainingSessionState.ExerciseState)?.exercise?.restTime?.isNotZero() == true
}