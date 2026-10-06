package com.lukasz.witkowski.training.planner.training.infrastructure.models

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlin.time.Instant

@Entity(tableName = "TrainingPlan")
data class DbTrainingPlan(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val restTime: Long,
    val lastModified: Instant,
    val lastUsed: Instant?,
)
