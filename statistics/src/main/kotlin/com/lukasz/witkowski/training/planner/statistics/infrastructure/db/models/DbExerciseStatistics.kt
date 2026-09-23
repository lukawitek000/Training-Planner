package com.lukasz.witkowski.training.planner.statistics.infrastructure.db.models

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "ExerciseStatistics")
data class DbExerciseStatistics(
    @PrimaryKey
    val id: String,
    val trainingStatisticsId: String,
    val trainingExerciseId: String
)
