package com.lukasz.witkowski.training.planner.analytics.domain.models

import com.lukasz.witkowski.training.planner.session.domain.models.SessionRpe
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.shared.utils.Weight
import com.lukasz.witkowski.training.planner.shared.utils.calculateCompletenessPercentage
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import java.util.Date
import kotlin.time.Duration

data class TrainingStatisticsOverview(
    val id: TrainingStatisticsId,
    val trainingPlanId: TrainingPlanId,
    val date: Date,
    val totalTime: Duration,
    val totalVolume: Weight,
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
