package com.lukasz.witkowski.training.planner.training.infrastructure.models

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.PrimaryKey

@Entity(
    tableName = "exercise_categories",
    primaryKeys = ["itemId"],
    foreignKeys = [
        ForeignKey(
            entity = DbExerciseCategory::class,
            parentColumns = ["trainingId", "exerciseId", "position"],
            childColumns = ["exerciseId", "trainingPlanId", "position"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DbExerciseCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val exerciseId: String,
    val trainingPlanId: String,
    val position: Int,
    val name: String
)
