package com.lukasz.witkowski.training.planner.analytics.infrastructure.db.models

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "ExerciseStatistics",
    indices = [
        Index(value = ["trainingStatisticsId"])
    ]
)
data class DbExerciseStatistics(
    @PrimaryKey
    val id: String,
    val trainingStatisticsId: String,
    val trainingExerciseId: String,
    val exerciseName: String = "",
    val plannedSets: Int = 1,
    val plannedReps: Int = 1,
    val plannedWeightInKg: Double? = null,
    val plannedHoldTime: Long? = null,
    val plannedRestTime: Long = 0L
)
