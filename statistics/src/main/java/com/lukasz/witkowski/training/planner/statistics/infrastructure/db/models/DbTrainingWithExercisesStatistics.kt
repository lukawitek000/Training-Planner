package com.lukasz.witkowski.training.planner.statistics.infrastructure.db.models

import androidx.room.Embedded
import androidx.room.Relation

data class DbTrainingWithExercisesStatistics(
    @Embedded val trainingStatistics: DbTrainingStatistics,
    @Relation(
        parentColumn = "id",
        entityColumn = "trainingStatisticsId",
        entity = DbExerciseStatistics::class
    )
    val exercisesStatistics: List<DbExerciseWithAttemptsStatistics>
)
