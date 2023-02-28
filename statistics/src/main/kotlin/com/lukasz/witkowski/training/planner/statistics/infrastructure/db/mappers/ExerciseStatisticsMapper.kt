package com.lukasz.witkowski.training.planner.statistics.infrastructure.db.mappers

import com.lukasz.witkowski.training.planner.statistics.domain.models.ExerciseStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.models.ExerciseStatisticsId
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.models.DbExerciseStatistics
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.models.DbExerciseWithAttemptsStatistics
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId

internal fun ExerciseStatistics.toDbExerciseStatistics(
    trainingStatisticsId: TrainingStatisticsId
): DbExerciseWithAttemptsStatistics {
    return DbExerciseWithAttemptsStatistics(
        exerciseStatistics = DbExerciseStatistics(
            id = id.toString(),
            trainingStatisticsId = trainingStatisticsId.toString(),
            trainingExerciseId = trainingExerciseId.toString()
        ),
        exerciseAttemptsStatistics = attemptsStatistics.map {
            it.toDbExerciseAttemptStatistics(id)
        }
    )
}

internal fun DbExerciseWithAttemptsStatistics.toExerciseStatistics(): ExerciseStatistics {
    return ExerciseStatistics(
        id = ExerciseStatisticsId(exerciseStatistics.id),
        trainingExerciseId = TrainingExerciseId(exerciseStatistics.trainingExerciseId),
        attemptsStatistics = exerciseAttemptsStatistics.map {
            it.toExerciseAttemptStatistics()
        }
    )
}
