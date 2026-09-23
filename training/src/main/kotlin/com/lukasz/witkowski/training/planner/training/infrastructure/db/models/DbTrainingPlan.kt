package com.lukasz.witkowski.training.planner.training.infrastructure.db.models

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "TrainingPlan")
data class DbTrainingPlan(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val isSynchronized: Boolean,
)
