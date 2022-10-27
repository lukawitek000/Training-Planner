package com.lukasz.witkowski.training.planner.statistics.domain.models

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId

data class ExerciseStatistics(
    val id: ExerciseStatisticsId = ExerciseStatisticsId.create(),
    val trainingExerciseId: TrainingExerciseId,
    val attemptsStatistics: List<ExerciseAttemptStatistics>
) {
    val totalTime: Time
        get() {
            val timeInMillis = attemptsStatistics.sumOf { it.time.timeInMillis }
            return Time(timeInMillis)
        }

    val completenessRate: Double
        get() {
            val numberOfAttempts = attemptsStatistics.size
            val completedAttempts = attemptsStatistics.count { it.completed }
            return completedAttempts / numberOfAttempts.toDouble()
        }
}
