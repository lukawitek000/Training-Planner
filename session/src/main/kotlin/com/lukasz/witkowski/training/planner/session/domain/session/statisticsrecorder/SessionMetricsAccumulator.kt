package com.lukasz.witkowski.training.planner.session.domain.session.statisticsrecorder

import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatisticsId
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
import java.util.UUID
import kotlin.time.Duration

internal data class SessionMetricsAccumulator(
    val statisticsId: TrainingStatisticsId = TrainingStatisticsId.create(),
    val trainingPlanId: TrainingPlanId,
    val sessionDate: Date,
    val startElapsed: Duration,
    val stopElapsed: Duration? = null,
    val attempts: List<ExerciseAttemptStatistics> = emptyList(),
    val exerciseStatisticsIds: Map<TrainingExerciseId, ExerciseStatisticsId> = emptyMap(),
    val exerciseNames: Map<TrainingExerciseId, String> = emptyMap(),
    val setCountsByExercise: Map<TrainingExerciseId, Int> = emptyMap(),
    val currentExerciseStartTime: Duration? = null,
    val currentRestStartTime: Duration? = null,
    val overallRpe: SessionRpe? = null,
    val notes: String? = null
) {
    fun reduce(
        event: SessionEvent,
        currentElapsed: Duration,
        currentDate: Date
    ): SessionMetricsAccumulator = when (event) {
        is SessionEvent.SessionStarted -> {
            start(event, currentElapsed, currentDate)
        }
        is SessionEvent.ExerciseStarted -> {
            copy(currentExerciseStartTime = currentElapsed)
        }
        is SessionEvent.ExerciseCompleted -> {
            recordAttempt(event.exercise, completed = true, currentElapsed = currentElapsed)
        }
        is SessionEvent.ExerciseSkipped -> {
            recordAttempt(event.exercise, completed = false, currentElapsed = currentElapsed)
        }
        is SessionEvent.RestStarted -> {
            copy(currentRestStartTime = currentElapsed)
        }
        is SessionEvent.RestCompleted -> {
            recordRestFinished(event.feedback, currentElapsed)
        }
        is SessionEvent.RestSkipped -> {
            recordRestFinished(event.feedback, currentElapsed)
        }
        is SessionEvent.ExtraSetAdded -> this
        is SessionEvent.SessionFinished -> {
            copy(stopElapsed = currentElapsed)
        }
        is SessionEvent.SummaryCompleted -> {
            copy(
                overallRpe = event.overallRpe,
                notes = event.notes?.ifBlank { null }
            )
        }
    }

    private fun recordAttempt(
        exercise: SessionExercise,
        completed: Boolean,
        currentElapsed: Duration
    ): SessionMetricsAccumulator {
        val exerciseId = exercise.exerciseId
        val setNumber = (setCountsByExercise[exerciseId] ?: 0) + 1
        val updatedSetCounts = setCountsByExercise + (exerciseId to setNumber)
        val attemptDuration = currentExerciseStartTime?.let { start ->
            if (currentElapsed >= start) currentElapsed - start else Duration.ZERO
        } ?: Duration.ZERO
        val deterministicAttemptId = ExerciseAttemptStatisticsId(
            UUID.nameUUIDFromBytes(
                "${statisticsId.value}_attempt_${exerciseId.value}_$setNumber".toByteArray()
            ).toString()
        )
        val attempt = ExerciseAttemptStatistics(
            id = deterministicAttemptId,
            trainingExerciseId = exerciseId,
            time = attemptDuration,
            set = setNumber,
            completed = completed,
            actualReps = exercise.currentConfig.reps,
            actualWeightInKg = exercise.currentConfig.weightInKg,
            actualTime = exercise.currentConfig.holdTime
        )
        val updatedExerciseIds = if (exerciseId !in exerciseStatisticsIds) {
            val deterministicExerciseStatsId = ExerciseStatisticsId(
                UUID.nameUUIDFromBytes(
                    "${statisticsId.value}_exercise_${exerciseId.value}".toByteArray()
                ).toString()
            )
            exerciseStatisticsIds + (exerciseId to deterministicExerciseStatsId)
        } else {
            exerciseStatisticsIds
        }

        return copy(
            attempts = attempts + attempt,
            setCountsByExercise = updatedSetCounts,
            exerciseStatisticsIds = updatedExerciseIds,
            exerciseNames = exerciseNames + (exerciseId to exercise.snapshot.name),
            currentExerciseStartTime = null
        )
    }

    private fun recordRestFinished(
        feedback: SetEffort?,
        currentElapsed: Duration
    ): SessionMetricsAccumulator {
        if (attempts.isEmpty()) return this
        val restDuration = currentRestStartTime?.let { start ->
            if (currentElapsed >= start) currentElapsed - start else Duration.ZERO
        } ?: Duration.ZERO
        val lastIndex = attempts.lastIndex
        val lastAttempt = attempts[lastIndex]
        val updatedLastAttempt = lastAttempt.copy(
            effort = feedback ?: lastAttempt.effort,
            restTime = restDuration
        )
        val updatedAttempts = attempts.toMutableList().apply {
            this[lastIndex] = updatedLastAttempt
        }
        return copy(
            attempts = updatedAttempts,
            currentRestStartTime = null
        )
    }

    fun toTrainingStatistics(currentElapsed: Duration): TrainingStatistics {
        val endTime = stopElapsed ?: currentElapsed
        val totalTime = if (endTime >= startElapsed) endTime - startElapsed else Duration.ZERO
        val exercisesStatisticsMap = attempts.groupBy { it.trainingExerciseId }
        val exercisesStatistics = exercisesStatisticsMap.map { (exerciseId, attemptsStatistics) ->
            val exerciseStatsId = exerciseStatisticsIds[exerciseId] ?: ExerciseStatisticsId(
                UUID.nameUUIDFromBytes(
                    "${statisticsId.value}_exercise_${exerciseId.value}".toByteArray()
                ).toString()
            )
            ExerciseStatistics(
                id = exerciseStatsId,
                trainingExerciseId = exerciseId,
                exerciseName = exerciseNames[exerciseId].orEmpty(),
                attemptsStatistics = attemptsStatistics
            )
        }
        return TrainingStatistics(
            id = statisticsId,
            trainingPlanId = trainingPlanId,
            totalTime = totalTime,
            date = sessionDate,
            exercisesStatistics = exercisesStatistics,
            note = notes,
            rpe = overallRpe
        )
    }

    companion object {
        fun start(
            event: SessionEvent.SessionStarted,
            currentElapsed: Duration,
            currentDate: Date
        ): SessionMetricsAccumulator = SessionMetricsAccumulator(
            statisticsId = TrainingStatisticsId.create(),
            trainingPlanId = event.trainingPlanId,
            sessionDate = currentDate,
            startElapsed = currentElapsed
        )
    }
}
