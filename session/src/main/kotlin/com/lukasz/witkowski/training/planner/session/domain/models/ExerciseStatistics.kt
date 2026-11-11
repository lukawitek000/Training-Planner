package com.lukasz.witkowski.training.planner.session.domain.models

import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import kotlin.time.Duration

data class ExerciseStatistics(
    val id: ExerciseStatisticsId = ExerciseStatisticsId.create(),
    val trainingExerciseId: TrainingExerciseId,
    val attemptsStatistics: List<ExerciseAttemptStatistics>
) {
    val totalTime: Duration
        get()  = attemptsStatistics.fold(Duration.ZERO) { acc, value -> acc + value.time }

    val completenessRate: Double
        get() {
            val numberOfAttempts = attemptsStatistics.size
            val completedAttempts = attemptsStatistics.count { it.completed }
            return completedAttempts / numberOfAttempts.toDouble()
        }
}
