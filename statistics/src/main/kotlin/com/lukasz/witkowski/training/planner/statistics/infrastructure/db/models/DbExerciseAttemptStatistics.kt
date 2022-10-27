package com.lukasz.witkowski.training.planner.statistics.infrastructure.db.models

import androidx.room.Entity
import androidx.room.PrimaryKey

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
