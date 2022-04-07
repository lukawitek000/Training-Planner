package com.lukasz.witkowski.training.planner.statistics.presentation

import com.lukasz.witkowski.training.planner.training.presentation.TrainingExercise

sealed class TrainingSessionState(val time: Long) {

    object IdleState : TrainingSessionState(0L)

    data class ExerciseState(val exercise: TrainingExercise) : TrainingSessionState(exercise.time)

    data class RestTimeState(val restTime: Long) : TrainingSessionState(restTime)

    // TODO summary objects (Statistics, TrainingPlan??)
    data class SummaryState(val summary: String) : TrainingSessionState(0L)
}