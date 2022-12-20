package com.lukasz.witkowski.training.planner.statistics.infrastructure.db.mappers

import com.lukasz.witkowski.training.planner.statistics.domain.models.ExerciseStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.models.ExerciseStatisticsId
import com.lukasz.witkowski.training.planner.statistics.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.models.DbExerciseStatistics
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.models.DbExerciseWithAttemptsStatistics
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId

object ExerciseStatisticsMapper {

    fun toDbExerciseStatistics(
        exerciseStatistics: ExerciseStatistics,
        trainingStatisticsId: TrainingStatisticsId
    ): DbExerciseWithAttemptsStatistics {
        return DbExerciseWithAttemptsStatistics(
            exerciseStatistics = DbExerciseStatistics(
                id = exerciseStatistics.id.value,
                trainingStatisticsId = trainingStatisticsId.value,
                trainingExerciseId = exerciseStatistics.trainingExerciseId.toString()
            ),
            exerciseAttemptsStatistics = exerciseStatistics.attemptsStatistics.map {
                ExerciseAttemptStatisticsMapper.toDbExerciseAttemptStatistics(
                    it,
                    exerciseStatistics.id
                )
            }
        )
    }

    fun toExerciseStatistics(dbExerciseWithAttemptsStatistics: DbExerciseWithAttemptsStatistics): ExerciseStatistics {
        return ExerciseStatistics(
            id = ExerciseStatisticsId(dbExerciseWithAttemptsStatistics.exerciseStatistics.id),
            trainingExerciseId = TrainingExerciseId(dbExerciseWithAttemptsStatistics.exerciseStatistics.trainingExerciseId),
            attemptsStatistics = dbExerciseWithAttemptsStatistics.exerciseAttemptsStatistics.map {
                ExerciseAttemptStatisticsMapper.toExerciseAttemptStatistics(it)
            }
        )
    }
}
