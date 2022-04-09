package com.lukasz.witkowski.training.planner.statistics.infrastructure

import com.lukasz.witkowski.training.planner.statistics.domain.ExerciseStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.ExerciseStatisticsId
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.DbExerciseStatistics
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId

object ExerciseStatisticsMapper {

    fun toDbExerciseStatistics(exerciseStatistics: ExerciseStatistics): DbExerciseStatistics {
        return DbExerciseStatistics(
            id = exerciseStatistics.id.value,
            trainingStatisticsId = exerciseStatistics,
            trainingExerciseId = exerciseStatistics.trainingExerciseId.value,
            exerciseAttemptsStatistics = exerciseStatistics.attemptsStatistics.map {
                ExerciseAttemptStatisticsMapper.toDbExerciseAttemptStatistics(
                    it
                )
            }
        )
    }

    fun toExerciseStatistics(dbExerciseStatistics: DbExerciseStatistics): ExerciseStatistics {
        return ExerciseStatistics(
            id = ExerciseStatisticsId(dbExerciseStatistics.id),
            trainingExerciseId = TrainingExerciseId(dbExerciseStatistics.trainingExerciseId),
            attemptsStatistics = dbExerciseStatistics.exerciseAttemptsStatistics.map {
                ExerciseAttemptStatisticsMapper.toExerciseAttemptStatistics(
                    it
                )
            }
        )
    }

}