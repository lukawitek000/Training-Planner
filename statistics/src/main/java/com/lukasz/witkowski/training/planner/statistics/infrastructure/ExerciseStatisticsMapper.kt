package com.lukasz.witkowski.training.planner.statistics.infrastructure

import com.lukasz.witkowski.training.planner.statistics.domain.ExerciseStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.ExerciseStatisticsId
import com.lukasz.witkowski.training.planner.statistics.domain.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.DbExerciseStatistics
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.DbExerciseWithAttemptsStatistics
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
            trainingExerciseId = exerciseStatistics.trainingExerciseId.value
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