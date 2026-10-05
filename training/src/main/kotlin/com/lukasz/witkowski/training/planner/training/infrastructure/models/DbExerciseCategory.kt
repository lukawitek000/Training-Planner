package com.lukasz.witkowski.training.planner.training.infrastructure.models

import androidx.room3.Entity

@Entity(
    tableName = "exercise_categories",
    primaryKeys = ["trainingExerciseId", "name"]
)
data class DbExerciseCategory(
    val trainingExerciseId: String,
    val trainingPlanId: String,
    val name: String
)
