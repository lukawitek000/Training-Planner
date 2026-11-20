package com.lukasz.witkowski.training.planner.analytics.domain.models

import com.lukasz.witkowski.training.planner.session.domain.models.SessionRpe
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.shared.utils.calculateCompletenessPercentage
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlin.time.Duration
import kotlin.time.Instant

data class TrainingStatisticsOverview(
    val id: TrainingStatisticsId,
    val trainingPlanId: TrainingPlanId,
    val date: Instant,
    val totalTime: Duration,
    val totalVolumeKg: Double,
    val completedSetsCount: Int,
    val totalSetsCount: Int,
    val rpe: SessionRpe?,
    val note: String?,
    val exerciseOverviews: List<ExerciseStatisticsOverview>
) {
    val completenessRate: Double
        get() = calculateCompletenessPercentage(
            completed = completedSetsCount,
            total = totalSetsCount
        )
}
