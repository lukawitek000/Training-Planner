package com.lukasz.witkowski.training.planner.statistics.presentation

import com.lukasz.witkowski.training.planner.training.presentation.TrainingExercise

sealed class TrainingSessionState(val exercise: TrainingExercise? = null, val time: Long = 0L) {

    object IdleState : TrainingSessionState()

    class ExerciseState(currentExercise: TrainingExercise) : TrainingSessionState(currentExercise, currentExercise.time)

    class RestTimeState(nextExercise: TrainingExercise, val restTime: Long) : TrainingSessionState(nextExercise, restTime)

    // TODO summary objects (Statistics, TrainingPlan??)
    data class SummaryState(val summary: String) : TrainingSessionState()
}