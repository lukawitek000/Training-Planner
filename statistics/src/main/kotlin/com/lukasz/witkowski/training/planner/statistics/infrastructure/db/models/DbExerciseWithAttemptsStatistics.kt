package com.lukasz.witkowski.training.planner.statistics.infrastructure.db.models

import androidx.room3.Embedded
import androidx.room3.Relation

data class DbExerciseWithAttemptsStatistics(
    @Embedded val exerciseStatistics: DbExerciseStatistics,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["exerciseStatisticsId"],
    )
    val exerciseAttemptsStatistics: List<DbExerciseAttemptStatistics>,
)
