package com.lukasz.witkowski.training.planner.training.infrastructure.db.models

data class DbExercise(
    val exerciseId: String,
    val name: String,
    val description: String,
    val category: Int,
    val imageId: String?
)
