package com.lukasz.witkowski.training.planner.analytics.infrastructure.db.mappers

import com.lukasz.witkowski.training.planner.analytics.infrastructure.db.models.DbTrainingStatistics
import com.lukasz.witkowski.training.planner.analytics.infrastructure.db.models.DbTrainingWithExercisesStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.SessionRpe
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import java.util.Date
import kotlin.time.Duration.Companion.milliseconds

internal fun TrainingStatistics.toDbTrainingStatistics(): DbTrainingWithExercisesStatistics =
    DbTrainingWithExercisesStatistics(
        trainingStatistics =
        DbTrainingStatistics(
            id = id.toString(),
            trainingPlanId = trainingPlanId.toString(),
            totalTime = totalTime.inWholeMilliseconds,
            date = date.time,
            note = note,
            rpe = rpe?.score
        ),
        exercisesStatistics = exercisesStatistics.map { it.toDbExerciseStatistics(id) }
    )

internal fun DbTrainingWithExercisesStatistics.toTrainingStatistics(): TrainingStatistics =
    TrainingStatistics(
        id = TrainingStatisticsId(trainingStatistics.id),
        trainingPlanId = TrainingPlanId(trainingStatistics.trainingPlanId),
        totalTime = trainingStatistics.totalTime.milliseconds,
        date = Date(trainingStatistics.date),
        exercisesStatistics = exercisesStatistics.map { it.toExerciseStatistics() },
        note = trainingStatistics.note,
        rpe = SessionRpe.fromScore(trainingStatistics.rpe)
    )
