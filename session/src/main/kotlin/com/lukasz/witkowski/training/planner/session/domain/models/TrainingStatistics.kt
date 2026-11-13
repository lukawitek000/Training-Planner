package com.lukasz.witkowski.training.planner.session.domain.models

import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import java.util.Date
import kotlin.time.Duration

data class TrainingStatistics(
    val id: TrainingStatisticsId = TrainingStatisticsId.create(),
    val trainingPlanId: TrainingPlanId,
    val totalTime: Duration,
    val date: Date,
    val exercisesStatistics: List<ExerciseStatistics>,
    val note: String? = null,
    val rpe: SessionRpe? = null
) {
    val effectiveTime: Duration
        get() = exercisesStatistics.fold(Duration.ZERO) { acc, statistics ->
            acc + statistics.totalTime
        }

    val allAttempts: List<ExerciseAttemptStatistics>
        get() = exercisesStatistics.flatMap { it.attemptsStatistics }

    val totalVolumeKg: Double
        get() = allAttempts.sumOf { (it.actualWeightInKg ?: 0.0) * it.actualReps }

    val totalReps: Int
        get() = allAttempts.sumOf { it.actualReps }

    val completedSetsCount: Int
        get() = allAttempts.count { it.completed }

    val totalSetsCount: Int
        get() = allAttempts.size
}
