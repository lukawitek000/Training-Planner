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
    val rpe: Int? = null
) {
    val effectiveTime: Time
        get() {
            val timeInMillis =
                exercisesStatistics.sumOf {
                    it.totalTime.timeInMillis
                }
            return Time(timeInMillis)
        }
}
