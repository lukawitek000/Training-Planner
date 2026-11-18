package com.lukasz.witkowski.training.planner.analytics.infrastructure.db.models

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "TrainingStatistics",
    indices = [
        Index(value = ["trainingPlanId", "date"])
    ]
)
data class DbTrainingStatistics(
    @PrimaryKey
    val id: String,
    val trainingPlanId: String,
    val totalTime: Long,
    val date: Long,
    val note: String? = null,
    val rpe: Int? = null
)
