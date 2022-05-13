package com.lukasz.witkowski.training.planner.statistics.presentation

import com.lukasz.witkowski.training.planner.statistics.domain.TrainingSessionState
import com.lukasz.witkowski.training.planner.training.presentation.mappers.TrainingExerciseMapper
import com.lukasz.witkowski.training.planner.training.presentation.mappers.TrainingPlanMapper

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
                state.statistics, TrainingPlanMapper.toPresentationTrainingPlan(state.trainingPlan)
            )
            is TrainingSessionState.IdleState -> com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionState.IdleState
        }
    }

}