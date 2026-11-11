package com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder

import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseStatisticsId
import com.lukasz.witkowski.training.planner.session.domain.models.SessionRpe
import com.lukasz.witkowski.training.planner.session.domain.models.SetEffort
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import java.util.Date
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * [BasicStatisticsRecorder] records standard session metrics:
 *  * Total training time
 *  * Exercise attempt durations
 *  * Attempt sets, reps, load, hold time, rest time, RPE, and completion status
 *  * Date of the workout session
 */
class BasicStatisticsRecorder(
    private var trainingPlanId: TrainingPlanId? = null,
    private val timeProvider: TimeProvider = SystemTimeProvider()
) : SessionStatisticsRecorder,
    SessionStatisticsProvider {
    private var statisticsId: TrainingStatisticsId = TrainingStatisticsId.create()
    private var startTrainingTime = timeProvider.currentTime()
    private var stopTrainingTime: Long? = null
    private var sessionDate: Date = timeProvider.currentDate()
    private val exercisesAttemptsStatistics = mutableListOf<ExerciseAttemptStatistics>()
    private val exerciseStatisticsIds = mutableMapOf<TrainingExerciseId, ExerciseStatisticsId>()
    private val setCountsByExercise = mutableMapOf<TrainingExerciseId, Int>()
    private var currentExerciseStartTime = 0L
    private var currentRestStartTime = 0L
    private var overallRpe: SessionRpe? = null
    private var notes: String? = null

    override fun onEvent(event: SessionEvent) {
        when (event) {
            is SessionEvent.SessionStarted -> {
                statisticsId = TrainingStatisticsId.create()
                trainingPlanId = event.trainingPlanId
                startTrainingTime = timeProvider.currentTime()
                stopTrainingTime = null
                sessionDate = timeProvider.currentDate()
                exercisesAttemptsStatistics.clear()
                exerciseStatisticsIds.clear()
                setCountsByExercise.clear()
                overallRpe = null
                notes = null
            }

            is SessionEvent.ExerciseStarted -> {
                currentExerciseStartTime = timeProvider.currentTime()
            }

            is SessionEvent.ExerciseCompleted -> {
                recordExerciseAttempt(event.exercise, completed = true)
            }

            is SessionEvent.ExerciseSkipped -> {
                recordExerciseAttempt(event.exercise, completed = false)
            }

            is SessionEvent.RestStarted -> {
                currentRestStartTime = timeProvider.currentTime()
            }

            is SessionEvent.RestCompleted -> {
                recordRestFinished(event.feedback)
            }

            is SessionEvent.RestSkipped -> {
                recordRestFinished(event.feedback)
            }

            is SessionEvent.ExtraSetAdded -> Unit

            is SessionEvent.SessionFinished -> {
                stopTrainingTime = timeProvider.currentTime()
            }

            is SessionEvent.SummaryCompleted -> {
                overallRpe = event.overallRpe
                notes = event.notes?.ifBlank { null }
            }
        }
    }

    override fun getStatistics(): TrainingStatistics {
        val endTime = stopTrainingTime ?: timeProvider.currentTime()
        val exercisesStatisticsMap = exercisesAttemptsStatistics.groupBy { it.trainingExerciseId }
        val exercisesStatistics =
            exercisesStatisticsMap.map { (exerciseId, attemptsStatistics) ->
                ExerciseStatistics(
                    id = exerciseStatisticsIds.getOrPut(exerciseId) {
                        ExerciseStatisticsId.create()
                    },
                    trainingExerciseId = exerciseId,
                    attemptsStatistics = attemptsStatistics
                )
            }
        return TrainingStatistics(
            id = statisticsId,
            trainingPlanId = trainingPlanId ?: TrainingPlanId.create(),
            totalTime = (endTime - startTrainingTime).milliseconds,
            date = sessionDate,
            exercisesStatistics = exercisesStatistics,
            note = notes,
            rpe = overallRpe
        )
    }

    private fun recordExerciseAttempt(exercise: SessionExercise, completed: Boolean) {
        val stopTime = timeProvider.currentTime().milliseconds
        val setNumber = (setCountsByExercise[exercise.exerciseId] ?: 0) + 1
        setCountsByExercise[exercise.exerciseId] = setNumber
        val attempt =
            ExerciseAttemptStatistics(
                trainingExerciseId = exercise.exerciseId,
                time = stopTime - currentExerciseStartTime.milliseconds,
                set = setNumber,
                completed = completed,
                actualReps = exercise.currentConfig.reps,
                actualWeightInKg = exercise.currentConfig.weightInKg,
                actualTime = exercise.currentConfig.holdTime
            )
        exercisesAttemptsStatistics.add(attempt)
    }

    private fun recordRestFinished(feedback: SetEffort?) {
        if (exercisesAttemptsStatistics.isEmpty()) return
        val elapsedRest = timeProvider.currentTime() - currentRestStartTime
        val restDuration = elapsedRest.milliseconds
        val lastIndex = exercisesAttemptsStatistics.lastIndex
        val lastAttempt = exercisesAttemptsStatistics[lastIndex]
        exercisesAttemptsStatistics[lastIndex] =
            lastAttempt.copy(
                effort = feedback ?: lastAttempt.effort,
                restTime = restDuration
            )
    }
}
