package com.lukasz.witkowski.training.planner.session.infrastructure.notification

sealed interface NotificationProgress {
    data class WorkoutSegments(val completedSets: Int, val exerciseSetCounts: List<Int>) :
        NotificationProgress {
        val totalSets: Int
            get() = exerciseSetCounts.sum()
    }

    data class RestCountdown(val elapsedSeconds: Int, val totalSeconds: Int) : NotificationProgress

    data object Completed : NotificationProgress
}

sealed interface NotificationTimer {
    data object None : NotificationTimer

    data class Ready(val totalSeconds: Long) : NotificationTimer

    data class Running(val remainingSeconds: Long) : NotificationTimer

    data class Overflow(val overflowSeconds: Long) : NotificationTimer

    data object Finished : NotificationTimer
}

sealed interface TrainingNotificationUiState {
    data object Idle : TrainingNotificationUiState

    data class ActiveExercise(
        val exerciseName: String,
        val currentSet: Int,
        val totalSets: Int,
        val reps: Int,
        val weightInKg: Double?,
        val holdSeconds: Long?,
        val timer: NotificationTimer,
        val progress: NotificationProgress.WorkoutSegments
    ) : TrainingNotificationUiState

    data class Rest(
        val nextExerciseName: String,
        val nextSet: Int?,
        val nextTotalSets: Int?,
        val timer: NotificationTimer,
        val progress: NotificationProgress.RestCountdown?
    ) : TrainingNotificationUiState

    data class Summary(val progress: NotificationProgress = NotificationProgress.Completed) :
        TrainingNotificationUiState
}
