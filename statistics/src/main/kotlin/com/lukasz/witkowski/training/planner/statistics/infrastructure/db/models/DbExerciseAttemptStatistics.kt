package com.lukasz.witkowski.training.planner.statistics.infrastructure.db.models

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "ExerciseAttemptStatistics")
data class DbExerciseAttemptStatistics(
    @PrimaryKey
    val id: String,
    val exerciseStatisticsId: String,
    val trainingExerciseId: String,
    val time: Long,
    val set: Int,
    val completed: Boolean
)
