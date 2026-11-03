package com.lukasz.witkowski.training.planner.session.presentation

import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.presentation.TrainingSessionState as PresentationState

fun TrainingSessionState.toPresentationTrainingSessionState(): PresentationState {
    throw IllegalStateException("Not implemented")
//    val exercise = exercise?.toPresentationTrainingExercise()
//    return when (this) {
//        is TrainingSessionState.ExerciseState -> PresentationState.ExerciseState(exercise!!)
//        is TrainingSessionState.RestTimeState ->
//            PresentationState.RestTimeState(
//                exercise!!,
//                restTime,
//            )
//        is TrainingSessionState.SummaryState ->
//            PresentationState.SummaryState(
//                statistics,
//                trainingPlan.toPresentationTrainingPlan(),
//            )
//        is TrainingSessionState.IdleState -> PresentationState.IdleState
//    }
}
