package com.lukasz.witkowski.training.planner.statistics.presentation

import com.lukasz.witkowski.training.planner.statistics.application.TrainingSessionState
import com.lukasz.witkowski.training.planner.training.presentation.TrainingExerciseMapper

object TrainingSessionStateConverter {

    fun toPresentation(state: TrainingSessionState): com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionState {
        val exercise =
            state.exercise?.let { TrainingExerciseMapper.toPresentationTrainingExercise(it) }
        return when (state) {
            is TrainingSessionState.ExerciseState -> com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionState.ExerciseState(
                exercise!!
            )
            is TrainingSessionState.RestTimeState -> com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionState.RestTimeState(
                exercise!!,
                state.restTime
            )
            is TrainingSessionState.SummaryState -> com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionState.SummaryState(
                state.summary
            )
            is TrainingSessionState.IdleState -> com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionState.IdleState
        }
    }

}