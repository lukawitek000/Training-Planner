package com.lukasz.witkowski.training.planner.session.infrastructure.notification

import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.domain.session.queue.ExerciseQueueProgress
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerState
import com.lukasz.witkowski.training.planner.shared.utils.WeightUnit
import kotlin.time.Duration

class TrainingNotificationUiStateMapper {
    fun map(
        state: TrainingSessionState,
        weightUnit: WeightUnit = WeightUnit.KG
    ): TrainingNotificationUiState = when (state) {
        is TrainingSessionState.IdleState -> TrainingNotificationUiState.Idle
        is TrainingSessionState.ExerciseState -> {
            val config = state.sessionExercise.currentConfig
            TrainingNotificationUiState.ActiveExercise(
                exerciseName = state.sessionExercise.snapshot.name,
                currentSet = state.progress.currentExerciseSet,
                totalSets = state.progress.currentExerciseTotalSets,
                reps = config.reps,
                weight = config.weight?.takeIf { it.isPositive },
                weightUnit = weightUnit,
                holdSeconds = config.holdTime?.takeIf { it > Duration.ZERO }?.inWholeSeconds,
                timer = mapTimer(state.timerState),
                progress = mapWorkoutProgress(state.progress)
            )
        }
        is TrainingSessionState.RestTimeState ->
            TrainingNotificationUiState.Rest(
                nextExerciseName = state.nextExercise.snapshot.name,
                nextSet = state.nextSetQueueProgress?.currentExerciseSet,
                nextTotalSets = state.nextSetQueueProgress?.currentExerciseTotalSets,
                timer = mapTimer(state.timerState),
                progress = mapRestProgress(state.totalRestTime, state.timerState)
            )
        is TrainingSessionState.SummaryState ->
            TrainingNotificationUiState.Summary(
                progress = NotificationProgress.Completed
            )
    }

    private fun mapWorkoutProgress(
        progress: ExerciseQueueProgress
    ): NotificationProgress.WorkoutSegments {
        val validCounts = progress.exerciseSetCounts.filter { it > 0 }
        if (validCounts.isNotEmpty()) {
            val totalSets = validCounts.sum()
            val completedSets = (totalSets - progress.remainingSetsInWorkout - 1).coerceIn(
                0,
                totalSets
            )
            return NotificationProgress.WorkoutSegments(
                completedSets = completedSets,
                exerciseSetCounts = validCounts
            )
        }
        if (progress.totalExercises <= 0) {
            return NotificationProgress.WorkoutSegments(
                completedSets = 0,
                exerciseSetCounts = emptyList()
            )
        }
        val completed =
            (progress.totalExercises - progress.remainingExercises)
                .coerceIn(0, progress.totalExercises)
        return NotificationProgress.WorkoutSegments(
            completedSets = completed,
            exerciseSetCounts = List(progress.totalExercises) { 1 }
        )
    }

    private fun mapRestProgress(
        totalRestTime: Duration,
        timerState: TimerState
    ): NotificationProgress.RestCountdown? {
        val totalSeconds = totalRestTime.inWholeSeconds.toInt()
        if (totalSeconds <= 0) return null
        val elapsedSeconds =
            when (timerState) {
                is TimerState.Running ->
                    (totalSeconds - ceilSeconds(timerState.remaining).toInt())
                        .coerceIn(0, totalSeconds)
                is TimerState.OverflowRunning,
                is TimerState.Finished -> totalSeconds
                is TimerState.Ready,
                is TimerState.NotSet -> 0
            }
        return NotificationProgress.RestCountdown(
            elapsedSeconds = elapsedSeconds,
            totalSeconds = totalSeconds
        )
    }

    private fun mapTimer(timerState: TimerState): NotificationTimer = when (timerState) {
        is TimerState.NotSet -> NotificationTimer.None
        is TimerState.Ready ->
            NotificationTimer.Ready(
                totalSeconds = timerState.duration.inWholeSeconds.coerceAtLeast(0L)
            )
        is TimerState.Running ->
            NotificationTimer.Running(
                remainingSeconds = ceilSeconds(timerState.remaining)
            )
        is TimerState.OverflowRunning ->
            NotificationTimer.Overflow(
                overflowSeconds = timerState.overflow.absoluteValue.inWholeSeconds
            )
        is TimerState.Finished -> NotificationTimer.Finished
    }

    private fun ceilSeconds(duration: Duration): Long = if (duration <= Duration.ZERO) {
        0L
    } else {
        (duration.inWholeMilliseconds + MILLIS_PER_SECOND - 1L) / MILLIS_PER_SECOND
    }

    private companion object {
        const val MILLIS_PER_SECOND = 1000L
    }
}
