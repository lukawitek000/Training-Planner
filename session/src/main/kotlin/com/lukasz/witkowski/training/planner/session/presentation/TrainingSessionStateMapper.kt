package com.lukasz.witkowski.training.planner.session.presentation

import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState as DomainState
import com.lukasz.witkowski.training.planner.session.presentation.TrainingSessionState as PresentationState
import com.lukasz.witkowski.training.planner.shared.time.Time
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

fun DomainState.toPresentationTrainingSessionState(
    time: Time = Time.ZERO,
    isTimerRunning: Boolean = false,
    selectedReps: Int? = null,
    selectedWeightInKg: Double? = null,
    selectedHoldDuration: Duration? = null,
    selectedFeedback: Int? = null,
    summaryRpe: Int? = null,
    summaryNotes: String = ""
): PresentationState = when (this) {
    is DomainState.ExerciseState -> PresentationState.ExerciseState(
        currentExercise = currentExercise,
        currentSet = currentSet,
        totalSets = totalSets,
        targetReps = targetReps,
        targetWeightInKg = targetWeightInKg,
        targetTime = targetTime,
        completedSets = completedSets,
        exerciseIndex = exerciseIndex,
        totalExercises = totalExercises,
        setsLeftInWorkout = setsLeftInWorkout,
        followingExercise = followingExercise,
        selectedReps = selectedReps ?: targetReps,
        selectedWeightInKg = selectedWeightInKg ?: targetWeightInKg,
        selectedHoldDuration = selectedHoldDuration ?: targetTime,
        remainingHoldTime = if (isIsometric) time.timeInMillis.milliseconds else targetTime,
        isHoldTimerRunning = isTimerRunning
    )
    is DomainState.RestTimeState -> PresentationState.RestTimeState(
        nextExercise = nextExercise,
        restTime = time,
        nextSet = nextSet,
        totalSetsForNextExercise = totalSetsForNextExercise,
        lastCompletedAttempt = lastCompletedAttempt,
        followingExercise = followingExercise,
        selectedFeedback = selectedFeedback
    )
    is DomainState.SummaryState -> PresentationState.SummaryState(
        statistics = statistics,
        trainingPlan = trainingPlan,
        selectedOverallRpe = summaryRpe ?: selectedOverallRpe,
        notes = summaryNotes.ifEmpty { notes }
    )
    is DomainState.IdleState -> PresentationState.IdleState
}
