package com.lukasz.witkowski.training.planner.analytics.infrastructure.db.mappers

import com.lukasz.witkowski.training.planner.analytics.infrastructure.db.models.DbExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatisticsId
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseStatisticsId
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId

internal fun ExerciseAttemptStatistics.toDbExerciseAttemptStatistics(
    exerciseStatisticsId: ExerciseStatisticsId
): DbExerciseAttemptStatistics = DbExerciseAttemptStatistics(
    id = id.toString(),
    exerciseStatisticsId = exerciseStatisticsId.toString(),
    trainingExerciseId = trainingExerciseId.toString(),
    time = time.timeInMillis,
    set = set,
    completed = completed,
    actualReps = actualReps,
    actualWeightInKg = actualWeightInKg,
    rpe = rpe
)

internal fun DbExerciseAttemptStatistics.toExerciseAttemptStatistics(): ExerciseAttemptStatistics =
    ExerciseAttemptStatistics(
        id = ExerciseAttemptStatisticsId(id),
        trainingExerciseId = TrainingExerciseId(trainingExerciseId),
        time = Time(time),
        set = set,
        completed = completed,
        actualReps = actualReps,
        actualWeightInKg = actualWeightInKg,
        rpe = rpe
    )
