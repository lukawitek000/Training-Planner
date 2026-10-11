package com.lukasz.witkowski.training.planner.statistics.infrastructure.db.models

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "TrainingStatistics")
data class DbTrainingStatistics(
    @PrimaryKey
    val id: String,
    val trainingPlanId: String,
    val totalTime: Long,
    val date: Long
)
