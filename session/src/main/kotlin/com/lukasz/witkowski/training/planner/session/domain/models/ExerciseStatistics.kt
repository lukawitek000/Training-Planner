package com.lukasz.witkowski.training.planner.session.domain.models

import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import kotlin.time.Duration

data class ExerciseStatistics(
    val id: ExerciseStatisticsId = ExerciseStatisticsId.create(),
    val trainingExerciseId: TrainingExerciseId,
    val exerciseName: String = "",
    val attemptsStatistics: List<ExerciseAttemptStatistics>
) {
    val totalTime: Duration
        get() = attemptsStatistics.fold(Duration.ZERO) { acc, value -> acc + value.time }

    val completenessRate: Double
        get() {
            val numberOfAttempts = attemptsStatistics.size
            if (numberOfAttempts == 0) return 0.0
            val completedAttempts = attemptsStatistics.count { it.completed }
            return completedAttempts / numberOfAttempts.toDouble()
        }
}
