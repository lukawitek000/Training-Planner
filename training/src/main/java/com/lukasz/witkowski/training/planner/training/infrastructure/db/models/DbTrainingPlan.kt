package com.lukasz.witkowski.training.planner.training.infrastructure.db.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "TrainingPlan")
internal data class DbTrainingPlan(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val isSynchronized: Boolean
)
