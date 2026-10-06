package com.lukasz.witkowski.training.planner.training.infrastructure.models

data class DbExerciseSnapshot(
    val exerciseId: String,
    val name: String,
    val description: String,
    val category: Int,
    val imagePath: String?,
)
