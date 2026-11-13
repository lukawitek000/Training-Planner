package com.lukasz.witkowski.training.planner.analytics.infrastructure.db.mappers

import com.lukasz.witkowski.training.planner.analytics.domain.models.ExerciseStatisticsOverview
import com.lukasz.witkowski.training.planner.analytics.domain.models.TrainingStatisticsOverview
import com.lukasz.witkowski.training.planner.analytics.infrastructure.db.models.DbTrainingWithExercisesStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseStatisticsId
import com.lukasz.witkowski.training.planner.session.domain.models.SessionRpe
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import java.util.Date
import kotlin.time.Duration.Companion.milliseconds

internal fun DbTrainingWithExercisesStatistics.toTrainingStatisticsOverview():
    TrainingStatisticsOverview {
    var totalVolumeKg = 0.0
    var completedSetsCount = 0
    var totalSetsCount = 0

    val exerciseOverviews = exercisesStatistics.map { exerciseWithAttempts ->
        val attempts = exerciseWithAttempts.exerciseAttemptsStatistics
        var exerciseTimeMs = 0L
        var exerciseCompletedSets = 0

        for (attempt in attempts) {
            exerciseTimeMs += attempt.time
            if (attempt.completed) {
                exerciseCompletedSets++
            }
            totalVolumeKg += (attempt.actualWeightInKg ?: 0.0) * attempt.actualReps
        }

        completedSetsCount += exerciseCompletedSets
        totalSetsCount += attempts.size

        ExerciseStatisticsOverview(
            id = ExerciseStatisticsId(exerciseWithAttempts.exerciseStatistics.id),
            trainingExerciseId = TrainingExerciseId(
                exerciseWithAttempts.exerciseStatistics.trainingExerciseId
            ),
            exerciseName = exerciseWithAttempts.exerciseStatistics.exerciseName,
            totalTime = exerciseTimeMs.milliseconds,
            completedSetsCount = exerciseCompletedSets,
            totalSetsCount = attempts.size
        )
    }

    return TrainingStatisticsOverview(
        id = TrainingStatisticsId(trainingStatistics.id),
        trainingPlanId = TrainingPlanId(trainingStatistics.trainingPlanId),
        date = Date(trainingStatistics.date),
        totalTime = trainingStatistics.totalTime.milliseconds,
        totalVolumeKg = totalVolumeKg,
        completedSetsCount = completedSetsCount,
        totalSetsCount = totalSetsCount,
        rpe = SessionRpe.fromScore(trainingStatistics.rpe),
        note = trainingStatistics.note,
        exerciseOverviews = exerciseOverviews
    )
}
