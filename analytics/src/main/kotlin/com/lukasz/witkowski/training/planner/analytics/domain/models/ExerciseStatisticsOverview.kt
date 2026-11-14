package com.lukasz.witkowski.training.planner.analytics.domain.models

import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseStatisticsId
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import kotlin.time.Duration

data class ExerciseStatisticsOverview(
    val id: ExerciseStatisticsId,
    val trainingExerciseId: TrainingExerciseId,
    val exerciseName: String,
    val totalTime: Duration,
    val completedSetsCount: Int,
    val totalSetsCount: Int
) {
    val completenessRate: Double
        get() = if (totalSetsCount == 0) 0.0 else (completedSetsCount * 100.0) / totalSetsCount.toDouble()
}
