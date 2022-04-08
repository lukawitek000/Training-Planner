package com.lukasz.witkowski.training.planner.statistics.application

import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise


// TODO domain object needed for service
sealed class TrainingSessionState(val exercise: TrainingExercise? = null) {

    object IdleState : TrainingSessionState()

    class ExerciseState(currentExercise: TrainingExercise) : TrainingSessionState(currentExercise)

    class RestTimeState(nextExercise: TrainingExercise, val restTime: Long) : TrainingSessionState(nextExercise)

    // TODO summary objects (Statistics, TrainingPlan??)
    data class SummaryState(val summary: String) : TrainingSessionState()
}