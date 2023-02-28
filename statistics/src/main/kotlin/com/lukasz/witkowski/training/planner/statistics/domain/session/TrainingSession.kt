package com.lukasz.witkowski.training.planner.statistics.domain.session

import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan

/**
 * Training session aggregate - controls state of the training session.
 * Requires [TrainingPlan] with at least one [TrainingExercise].
 */
@Suppress("TooManyFunctions")
internal class TrainingSession(
    private val trainingPlan: TrainingPlan,
    private val trainingSetsPolicy: TrainingSetsPolicy
) {

    private val exercises = mutableListOf<TrainingExercise>()
    private var state: TrainingSessionState = TrainingSessionState.IdleState
    private val currentExercise: TrainingExercise
        get() = state.exercise!!

    private lateinit var exerciseSession: ExerciseSession
    private lateinit var trainingStatistics: TrainingStatistics

    init {
        require(trainingPlan.exercises.isNotEmpty()) { "Cannot start training session without exercises" }
        exercises.addAll(trainingSetsPolicy.loadExercises(trainingPlan))
    }

    fun start(startTime: Time): TrainingSessionState {
        trainingStatistics = TrainingStatistics(trainingPlan, startTime)
        val firstExercise = loadExercise()
        exerciseSession = ExerciseSession(firstExercise, startTime, 1)
        return TrainingSessionState.ExerciseState(firstExercise).also {
            state = it
        }
    }

    fun skip(time: Time): TrainingSessionState {
        stopRecordingExerciseStatistics(false, time)
        return next(time)
    }

    fun completed(time: Time): TrainingSessionState {
        stopRecordingExerciseStatistics(true, time)
        return next(time)
    }

    fun stop() {
        exercises.clear()
        state = TrainingSessionState.IdleState
    }

    private fun next(time: Time): TrainingSessionState {
        return when {
            isTrainingSessionFinished() && !isTrainingSessionStopped() -> {
                val trainingStatistics = this.trainingStatistics.gatherTrainingStatistics(time)
                TrainingSessionState.SummaryState(trainingStatistics, trainingPlan)
            }
            isExerciseState() && exerciseSession.hasRestTime() -> {
                val nextExercise = getNextExerciseOverview()
                TrainingSessionState.RestTimeState(nextExercise, currentExercise.restTime)
            }
            isRestTimeState() || (isExerciseState() && !exerciseSession.hasRestTime()) -> {
                val currentExercise = loadExercise()
                startRecordingExerciseStatistics(currentExercise, time)
                TrainingSessionState.ExerciseState(currentExercise)
            }
            else -> throw UnknownTrainingSessionStateException("Unknown training session state.")
        }.also { state = it }
    }

    private fun startRecordingExerciseStatistics(currentExercise: TrainingExercise, time: Time) {
        exerciseSession = ExerciseSession(currentExercise, time, getCurrentSet(currentExercise))
    }

    private fun stopRecordingExerciseStatistics(isCompleted: Boolean, time: Time) {
        if (isExerciseState()) {
            val exerciseAttemptStatistics = exerciseSession.stop(isCompleted, time)
            trainingStatistics.addExerciseAttemptStatistics(exerciseAttemptStatistics)
        }
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

    private fun isTrainingSessionStopped() = state is TrainingSessionState.IdleState
}
