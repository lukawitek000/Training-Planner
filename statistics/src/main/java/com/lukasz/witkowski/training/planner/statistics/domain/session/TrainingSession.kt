package com.lukasz.witkowski.training.planner.statistics.domain.session

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.statistics.domain.session.statisticsrecorder.BasicStatisticsRecorder
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan

/**
 * Training session aggregate - controls state of the training session.
 * Requires [TrainingPlan] with at least one [TrainingExercise].
 */
internal class TrainingSession(
    private val trainingPlan: TrainingPlan,
    private val trainingSetsPolicy: TrainingSetsPolicy
) {

    private val exercises = mutableListOf<TrainingExercise>()
    private var state: TrainingSessionState = TrainingSessionState.IdleState
    private val currentExercise: TrainingExercise
        get() = state.exercise!!

    private val exerciseSession = ExerciseSession()
    private val statisticsRecorder = BasicStatisticsRecorder(trainingPlan.id)
    private lateinit var startTime: Time

    init {
        require(trainingPlan.exercises.isNotEmpty()) { "Cannot start training session without exercises" }
        exercises.addAll(trainingSetsPolicy.loadExercises(trainingPlan))
    }

    fun start(startTime: Time): TrainingSessionState {
        statisticsRecorder.start(startTime)
        val firstExercise = loadExercise()
        statisticsRecorder.startRecordingExercise(firstExercise.id, 1, startTime)
        state = TrainingSessionState.ExerciseState(firstExercise)
        return state
    }

    fun skip(time: Time): TrainingSessionState {
        return next(false, time)
    }

    fun completed(time: Time): TrainingSessionState {
        return next(true, time)
    }

    private fun next(isCompleted: Boolean, time: Time): TrainingSessionState {
        stopRecordingExerciseStatistics(isCompleted, time)
        state = when {
            isTrainingSessionFinished() -> {
                val trainingStatistics = statisticsRecorder.stop(time)
                TrainingSessionState.SummaryState(trainingStatistics, trainingPlan)
            }
            isExerciseState() && hasCurrentExerciseRestTime() -> {
                val nextExercise = getNextExerciseOverview()
                TrainingSessionState.RestTimeState(nextExercise, currentExercise.restTime)
            }
            isRestTimeState() || (isExerciseState() && !hasCurrentExerciseRestTime()) -> {
                val currentExercise = loadExercise()
                startRecordingExerciseStatistics(currentExercise, time)
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

    private fun startRecordingExerciseStatistics(currentExercise: TrainingExercise, time: Time) {
        statisticsRecorder.startRecordingExercise(
            currentExercise.id,
            getCurrentSet(currentExercise),
            time
        )
    }

    private fun stopRecordingExerciseStatistics(isCompleted: Boolean, time: Time) {
        if (isExerciseState()) {
            statisticsRecorder.stopRecordingExercise(isCompleted, time)
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

    private fun hasCurrentExerciseRestTime() =
        (state as? TrainingSessionState.ExerciseState)?.exercise?.restTime?.isNotZero() == true
}
