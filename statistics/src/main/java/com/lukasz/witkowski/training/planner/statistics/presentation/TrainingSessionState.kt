package com.lukasz.witkowski.training.planner.statistics.presentation

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingExercise
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingPlan

sealed class TrainingSessionState(
    val exercise: TrainingExercise? = null,
    val time: Time = Time.NONE
) {

    object IdleState : TrainingSessionState()

    class ExerciseState(currentExercise: TrainingExercise) :
        TrainingSessionState(currentExercise, currentExercise.time)

    class RestTimeState(nextExercise: TrainingExercise, private val restTime: Time) :
        TrainingSessionState(nextExercise, restTime)

    data class SummaryState(val statistics: TrainingStatistics, val trainingPlan: TrainingPlan) :
        TrainingSessionState(time = statistics.totalTime)
}
