package com.lukasz.witkowski.training.planner.statistics.infrastructure.db.mappers

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.statistics.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.models.ExerciseAttemptStatisticsId
import com.lukasz.witkowski.training.planner.statistics.domain.models.ExerciseStatisticsId
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.models.DbExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId

internal fun ExerciseAttemptStatistics.toDbExerciseAttemptStatistics(exerciseStatisticsId: ExerciseStatisticsId): DbExerciseAttemptStatistics {
    return DbExerciseAttemptStatistics(
        id = id.toString(),
        exerciseStatisticsId = exerciseStatisticsId.toString(),
        trainingExerciseId = trainingExerciseId.toString(),
        time = time.timeInMillis,
        set = set,
        completed = completed
    )
}

internal fun DbExerciseAttemptStatistics.toExerciseAttemptStatistics(): ExerciseAttemptStatistics {
    return ExerciseAttemptStatistics(
        id = ExerciseAttemptStatisticsId(id),
        trainingExerciseId = TrainingExerciseId(id),
        time = Time(time),
        set = set,
        completed = completed
    )
}
