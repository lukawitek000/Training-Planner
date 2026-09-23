package com.lukasz.witkowski.training.planner.statistics.infrastructure.db.models

import androidx.room3.Embedded
import androidx.room3.Relation

data class DbTrainingWithExercisesStatistics(
    @Embedded val trainingStatistics: DbTrainingStatistics,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["trainingStatisticsId"],
        entity = DbExerciseStatistics::class,
    )
    val exercisesStatistics: List<DbExerciseWithAttemptsStatistics>,
)
