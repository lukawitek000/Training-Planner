package com.lukasz.witkowski.training.planner.statistics.domain

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan

sealed class TrainingSessionState(val exercise: TrainingExercise? = null) {

    object IdleState : TrainingSessionState()

    class ExerciseState(currentExercise: TrainingExercise) : TrainingSessionState(currentExercise)

    class RestTimeState(nextExercise: TrainingExercise, val restTime: Time) : TrainingSessionState(nextExercise)

    data class SummaryState(val statistics: TrainingStatistics, val trainingPlan: TrainingPlan) : TrainingSessionState()
}
