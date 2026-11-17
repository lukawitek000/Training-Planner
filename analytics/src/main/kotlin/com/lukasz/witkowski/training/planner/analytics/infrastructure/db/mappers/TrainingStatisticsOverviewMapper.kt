package com.lukasz.witkowski.training.planner.analytics.infrastructure.db.mappers

import com.lukasz.witkowski.training.planner.analytics.domain.models.ExerciseStatisticsOverview
import com.lukasz.witkowski.training.planner.analytics.domain.models.TrainingStatisticsOverview
import com.lukasz.witkowski.training.planner.analytics.infrastructure.db.models.DbTrainingWithExercisesStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseStatistics

internal fun DbTrainingWithExercisesStatistics.toTrainingStatisticsOverview():
    TrainingStatisticsOverview {
    val statistics = toTrainingStatistics()
    return TrainingStatisticsOverview(
        id = statistics.id,
        trainingPlanId = statistics.trainingPlanId,
        date = statistics.date,
        totalTime = statistics.totalTime,
        totalVolumeKg = statistics.totalVolumeKg,
        completedSetsCount = statistics.completedSetsCount,
        totalSetsCount = statistics.totalSetsCount,
        rpe = statistics.rpe,
        note = statistics.note,
        exerciseOverviews = statistics.exercisesStatistics.map { it.toExerciseStatisticsOverview() }
    )
}

private fun ExerciseStatistics.toExerciseStatisticsOverview(): ExerciseStatisticsOverview =
    ExerciseStatisticsOverview(
        id = id,
        trainingExerciseId = trainingExerciseId,
        exerciseName = exerciseName,
        totalTime = totalTime,
        completedSetsCount = completedSetsCount,
        totalSetsCount = totalSetsCount
    )
