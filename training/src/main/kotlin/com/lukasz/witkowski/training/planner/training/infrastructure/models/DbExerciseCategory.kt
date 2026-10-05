package com.lukasz.witkowski.training.planner.training.infrastructure.models

import androidx.room3.Entity

@Entity(
    tableName = "exercise_categories",
    primaryKeys = ["exerciseId", "trainingPlanId", "position", "name"]
)
data class DbExerciseCategory(
    val exerciseId: String,
    val trainingPlanId: String,
    val position: Int,
    val name: String
)
