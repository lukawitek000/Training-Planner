package com.lukasz.witkowski.training.planner.session.domain.models

import com.lukasz.witkowski.training.planner.shared.utils.calculateCompletenessPercentage
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlin.time.Duration
import kotlin.time.Instant

data class TrainingStatistics(
    val id: TrainingStatisticsId = TrainingStatisticsId.create(),
    val trainingPlanId: TrainingPlanId,
    val totalTime: Duration,
    val date: Instant,
    val exercisesStatistics: List<ExerciseStatistics>,
    val note: String? = null,
    val rpe: SessionRpe? = null
) {
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

    val completenessRate: Double
        get() = calculateCompletenessPercentage(
            completed = completedSetsCount,
            total = totalSetsCount
        )
}
