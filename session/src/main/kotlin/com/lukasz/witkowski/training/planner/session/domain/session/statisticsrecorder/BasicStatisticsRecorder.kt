package com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder

import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import java.util.Date

/**
 * [BasicStatisticsRecorder] records standard session metrics:
 *  * Total training time
 *  * Exercise and rest durations
 *  * Attempt sets, reps, load, and completion status
 *  * Date of the workout session
 */
class BasicStatisticsRecorder(
    private var trainingPlanId: TrainingPlanId? = null,
    private val timeProvider: TimeProvider = SystemTimeProvider()
) : SessionStatisticsRecorder {
    private var startTrainingTime = Time.ZERO
    private var sessionDate: Date = timeProvider.currentDate()
    private val exercisesAttemptsStatistics = mutableListOf<ExerciseAttemptStatistics>()
    private var currentExerciseStartTime = Time.ZERO
    private var currentExerciseId: TrainingExerciseId? = null
    private var currentExerciseSet: Int = 0

    override fun onEvent(event: SessionEvent) {
        when (event) {
            is SessionEvent.SessionStarted -> {
                trainingPlanId = event.trainingPlan.id
                startTrainingTime = event.startTime
                sessionDate = event.date
                exercisesAttemptsStatistics.clear()
            }
            is SessionEvent.ExerciseStarted -> {
                currentExerciseStartTime = event.startTime
                currentExerciseId = event.exercise.id
                currentExerciseSet = event.set
            }
            is SessionEvent.ExerciseCompleted -> {
                exercisesAttemptsStatistics.add(event.attempt)
            }
            is SessionEvent.ExerciseSkipped -> {
                // Skips can be tracked or inspected in future analytics
            }
            is SessionEvent.RestStarted -> {}
            is SessionEvent.RestCompleted -> {}
            is SessionEvent.RestSkipped -> {}
            is SessionEvent.ExtraSetAdded -> {}
            is SessionEvent.SessionFinished -> {}
        }
    }

    fun start(startTime: Time) {
        startTrainingTime = startTime
        sessionDate = timeProvider.currentDate()
    }

    fun stop(stopTime: Time): TrainingStatistics {
        val trainingStatistics = gatherTrainingStatistics(stopTime)
        exercisesAttemptsStatistics.clear()
        return trainingStatistics
    }

    fun startRecordingExercise(trainingExerciseId: TrainingExerciseId, set: Int, startTime: Time) {
        currentExerciseStartTime = startTime
        currentExerciseId = trainingExerciseId
        currentExerciseSet = set
    }

    fun stopRecordingExercise(isCompleted: Boolean, stopTime: Time) {
        val exerciseAttemptStatistics =
            ExerciseAttemptStatistics(
                trainingExerciseId = currentExerciseId!!,
                time = stopTime - currentExerciseStartTime,
                set = currentExerciseSet,
                completed = isCompleted
            )
        exercisesAttemptsStatistics.add(exerciseAttemptStatistics)
    }

    fun gatherTrainingStatistics(time: Time): TrainingStatistics {
        val exercisesStatisticsMap = exercisesAttemptsStatistics.groupBy { it.trainingExerciseId }
        val exercisesStatistics =
            exercisesStatisticsMap.map { (exerciseId, attemptsStatistics) ->
                ExerciseStatistics(
                    trainingExerciseId = exerciseId,
                    attemptsStatistics = attemptsStatistics
                )
            }
        return TrainingStatistics(
            trainingPlanId = trainingPlanId ?: TrainingPlanId.create(),
            totalTime = time - startTrainingTime,
            date = sessionDate,
            exercisesStatistics = exercisesStatistics
        )
    }
}
