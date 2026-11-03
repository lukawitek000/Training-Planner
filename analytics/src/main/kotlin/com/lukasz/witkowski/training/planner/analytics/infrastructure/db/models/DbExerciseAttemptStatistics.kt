package com.lukasz.witkowski.training.planner.analytics.infrastructure.db.models

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
    val completed: Boolean,
    val actualReps: Int = 0,
    val actualWeightInKg: Double? = null,
    val rpe: Float? = null
)
