package com.lukasz.witkowski.training.planner.statistics.infrastructure.db.mappers

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.statistics.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.statistics.domain.models.ExerciseAttemptStatisticsId
import com.lukasz.witkowski.training.planner.statistics.domain.models.ExerciseStatisticsId
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.models.DbExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId

object ExerciseAttemptStatisticsMapper {

    fun toDbExerciseAttemptStatistics(
        exerciseAttemptStatistics: ExerciseAttemptStatistics,
        exerciseStatisticsId: ExerciseStatisticsId
    ): DbExerciseAttemptStatistics {
        return DbExerciseAttemptStatistics(
            id = exerciseAttemptStatistics.id.value,
            exerciseStatisticsId = exerciseStatisticsId.value,
            trainingExerciseId = exerciseAttemptStatistics.trainingExerciseId.value,
            time = exerciseAttemptStatistics.time.timeInMillis,
            set = exerciseAttemptStatistics.set,
            completed = exerciseAttemptStatistics.completed
        )
    }

    fun toExerciseAttemptStatistics(dbExerciseAttemptStatistics: DbExerciseAttemptStatistics): ExerciseAttemptStatistics {
        return ExerciseAttemptStatistics(
            id = ExerciseAttemptStatisticsId(dbExerciseAttemptStatistics.id),
            trainingExerciseId = TrainingExerciseId(dbExerciseAttemptStatistics.id),
            time = Time(dbExerciseAttemptStatistics.time),
            set = dbExerciseAttemptStatistics.set,
            completed = dbExerciseAttemptStatistics.completed
        )
    }
}
