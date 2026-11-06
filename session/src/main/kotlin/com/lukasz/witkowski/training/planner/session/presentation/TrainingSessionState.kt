package com.lukasz.witkowski.training.planner.session.presentation

import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExercise
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import kotlin.time.Duration

sealed interface TrainingSessionState {
    data object IdleState : TrainingSessionState

    data class ExerciseState(
        val currentExercise: SessionExercise,
        val currentSet: Int,
        val totalSets: Int,
    ) : TrainingSessionState

    data class RestTimeState(
        val nextExercise: TrainingExercise,
        val restTime: Time,
        val nextSet: Int = 1,
        val totalSetsForNextExercise: Int = nextExercise.sets,
        val lastCompletedAttempt: ExerciseAttemptStatistics? = null,
        val followingExercise: TrainingExercise? = null,
        val selectedFeedback: Int? = null
    ) : TrainingSessionState

    data class SummaryState(
        val statistics: TrainingStatistics,
        val trainingPlan: TrainingPlan,
        val selectedOverallRpe: Int? = null,
        val notes: String = ""
    ) : TrainingSessionState
}
