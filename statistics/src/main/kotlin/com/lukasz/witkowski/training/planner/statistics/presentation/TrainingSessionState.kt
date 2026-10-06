package com.lukasz.witkowski.training.planner.statistics.presentation

import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan

sealed class TrainingSessionState(
    val exercise: TrainingExercise? = null,
    val time: Time = Time.ZERO,
) {
    object IdleState : TrainingSessionState()

    class ExerciseState(
        val currentExercise: TrainingExercise,
    ) : TrainingSessionState(currentExercise, Time.ZERO)

    class RestTimeState(
        nextExercise: TrainingExercise,
        private val restTime: Time,
    ) : TrainingSessionState(nextExercise, restTime)

    data class SummaryState(
        val statistics: TrainingStatistics,
        val trainingPlan: TrainingPlan,
    ) : TrainingSessionState(time = statistics.totalTime)
}
