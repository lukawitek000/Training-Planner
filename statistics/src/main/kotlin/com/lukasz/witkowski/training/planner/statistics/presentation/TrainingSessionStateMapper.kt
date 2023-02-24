package com.lukasz.witkowski.training.planner.statistics.presentation

import com.lukasz.witkowski.training.planner.statistics.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.training.presentation.mappers.TrainingPlanMapper
import com.lukasz.witkowski.training.planner.training.presentation.mappers.toPresentationTrainingExercise
import com.lukasz.witkowski.training.planner.training.presentation.mappers.toPresentationTrainingPlan
import com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionState as PresentationState

fun TrainingSessionState.toPresentationTrainingSessionState(
): PresentationState {
    val exercise = exercise?.toPresentationTrainingExercise()
    return when (this) {
        is TrainingSessionState.ExerciseState -> PresentationState.ExerciseState(exercise!!)
        is TrainingSessionState.RestTimeState -> PresentationState.RestTimeState(
            exercise!!,
            restTime
        )
        is TrainingSessionState.SummaryState -> PresentationState.SummaryState(
            statistics,
            trainingPlan.toPresentationTrainingPlan()
        )
        is TrainingSessionState.IdleState -> PresentationState.IdleState
    }
}
