package com.lukasz.witkowski.training.planner.session.presentation

import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExercise
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import kotlin.time.Duration

sealed class TrainingSessionState(
    open val exercise: TrainingExercise? = null,
    open val time: Time = Time.ZERO
) {
    data object IdleState : TrainingSessionState()

    data class ExerciseState(
        val currentExercise: TrainingExercise,
        val sessionExercise: SessionExercise? = null,
        val currentSet: Int = 1,
        val totalSets: Int = currentExercise.sets,
        val targetReps: Int = currentExercise.repetitions,
        val targetWeightInKg: Double? = currentExercise.weightInKg?.toDouble(),
        val targetTime: Duration = currentExercise.time,
        val completedSets: List<ExerciseAttemptStatistics> = emptyList(),
        val exerciseIndex: Int = 1,
        val totalExercises: Int = 1,
        val setsLeftInWorkout: Int = 0,
        val followingExercise: TrainingExercise? = null,
        val selectedReps: Int = targetReps,
        val selectedWeightInKg: Double? = targetWeightInKg,
        val selectedHoldDuration: Duration = targetTime,
        val remainingHoldTime: Duration = targetTime,
        val isHoldTimerRunning: Boolean = false
    ) : TrainingSessionState(currentExercise, Time.ZERO) {
        val isIsometric: Boolean
            get() = targetTime > Duration.ZERO
    }

    data class RestTimeState(
        val nextExercise: TrainingExercise,
        val restTime: Time,
        val nextSet: Int = 1,
        val totalSetsForNextExercise: Int = nextExercise.sets,
        val lastCompletedAttempt: ExerciseAttemptStatistics? = null,
        val followingExercise: TrainingExercise? = null,
        val selectedFeedback: Int? = null
    ) : TrainingSessionState(nextExercise, restTime)

    data class SummaryState(
        val statistics: TrainingStatistics,
        val trainingPlan: TrainingPlan,
        val selectedOverallRpe: Int? = null,
        val notes: String = ""
    ) : TrainingSessionState(time = statistics.totalTime)
}
