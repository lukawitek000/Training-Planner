package com.lukasz.witkowski.training.planner.session.domain.models

import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import java.util.Date

data class TrainingStatistics(
    val id: TrainingStatisticsId = TrainingStatisticsId.create(),
    val trainingPlanId: TrainingPlanId,
    val totalTime: Time,
    val date: Date,
    val exercisesStatistics: List<ExerciseStatistics>,
    val note: String? = null,
    val rpe: SessionRpe? = null
) {
    val effectiveTime: Time
        get() {
            val timeInMillis =
                exercisesStatistics.sumOf {
                    it.totalTime.timeInMillis
                }
            return Time(timeInMillis)
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
