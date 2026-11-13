package com.lukasz.witkowski.training.planner.analytics.infrastructure.db.mappers

import com.lukasz.witkowski.training.planner.analytics.infrastructure.db.models.DbExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatisticsId
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseStatisticsId
import com.lukasz.witkowski.training.planner.session.domain.models.SetEffort
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import kotlin.time.Duration.Companion.milliseconds

internal fun ExerciseAttemptStatistics.toDbExerciseAttemptStatistics(
    exerciseStatisticsId: ExerciseStatisticsId
): DbExerciseAttemptStatistics = DbExerciseAttemptStatistics(
    id = id.toString(),
    exerciseStatisticsId = exerciseStatisticsId.toString(),
    trainingExerciseId = trainingExerciseId.toString(),
    time = time.inWholeMilliseconds,
    set = set,
    completed = completed,
    actualReps = actualReps,
    actualWeightInKg = actualWeightInKg,
    actualTime = actualTime?.inWholeMilliseconds,
    rpe = effort?.score,
    restTime = restTime?.inWholeMilliseconds
)

internal fun DbExerciseAttemptStatistics.toExerciseAttemptStatistics(): ExerciseAttemptStatistics =
    ExerciseAttemptStatistics(
        id = ExerciseAttemptStatisticsId(id),
        trainingExerciseId = TrainingExerciseId(trainingExerciseId),
        time = time.milliseconds,
        set = set,
        completed = completed,
        actualReps = actualReps,
        actualWeightInKg = actualWeightInKg,
        actualTime = actualTime?.milliseconds,
        effort = SetEffort.fromNumeric(rpe),
        restTime = restTime?.milliseconds
    )
