package com.lukasz.witkowski.training.planner.session.presentation

import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState as DomainState
import com.lukasz.witkowski.training.planner.session.presentation.TrainingSessionState as PresentationState

fun DomainState.toPresentationTrainingSessionState(): PresentationState = when (this) {
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
        followingExerciseName = followingExerciseName
    )
    is DomainState.RestTimeState -> PresentationState.RestTimeState(
        nextExercise = nextExercise,
        restTime = restTime,
        nextSet = nextSet,
        totalSetsForNextExercise = totalSetsForNextExercise,
        lastCompletedAttempt = lastCompletedAttempt,
        followingExerciseName = followingExerciseName
    )
    is DomainState.SummaryState -> PresentationState.SummaryState(
        statistics = statistics,
        trainingPlan = trainingPlan,
        selectedOverallRpe = selectedOverallRpe,
        notes = notes
    )
    is DomainState.IdleState -> PresentationState.IdleState
}
