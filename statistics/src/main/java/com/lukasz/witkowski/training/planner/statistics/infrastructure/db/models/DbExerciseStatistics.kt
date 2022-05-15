package com.lukasz.witkowski.training.planner.statistics.infrastructure.db.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "ExerciseStatistics")
data class DbExerciseStatistics(
    @PrimaryKey
    val id: String,
    val trainingStatisticsId: String,
    val trainingExerciseId: String
)
