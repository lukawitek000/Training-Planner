package com.lukasz.witkowski.training.planner.statistics.infrastructure.db

import androidx.room.Embedded
import androidx.room.Relation

data class DbExerciseWithAttemptsStatistics(
    @Embedded val exerciseStatistics: DbExerciseStatistics,
    @Relation(
        parentColumn = "id",
        entityColumn = "exerciseStatisticsId"
    )
    val exerciseAttemptsStatistics: List<DbExerciseAttemptStatistics>
)
