package com.lukasz.witkowski.training.planner.session.domain.session

import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import kotlin.time.Duration

sealed class TrainingSessionState(val exercise: TrainingExercise? = null) {
    object IdleState : TrainingSessionState()

    class ExerciseState(
        val currentExercise: TrainingExercise,
        val currentSet: Int = 1,
        val totalSets: Int = currentExercise.sets,
        val targetReps: Int = currentExercise.repetitions,
        val targetWeightInKg: Double? = currentExercise.weightInKg?.toDouble(),
        val targetTime: Duration = currentExercise.time,
        val completedSets: List<ExerciseAttemptStatistics> = emptyList(),
        val exerciseIndex: Int = 1,
        val totalExercises: Int = 1,
        val setsLeftInWorkout: Int = 0,
        val followingExerciseName: String? = null
    ) : TrainingSessionState(currentExercise) {
        val isIsometric: Boolean
            get() = targetTime > Duration.ZERO
    }

    class RestTimeState(
        val nextExercise: TrainingExercise,
        val restTime: Time,
        val nextSet: Int = 1,
        val totalSetsForNextExercise: Int = nextExercise.sets,
        val lastCompletedAttempt: ExerciseAttemptStatistics? = null,
        val followingExerciseName: String? = null
    ) : TrainingSessionState(nextExercise)

    data class SummaryState(
        val statistics: TrainingStatistics,
        val trainingPlan: TrainingPlan,
        val selectedOverallRpe: Int? = null,
        val notes: String = ""
    ) : TrainingSessionState()
}
