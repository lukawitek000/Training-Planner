package com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi

data class TrainingPlanJsonModel(
    val id: String,
    val title: String,
    val description: String,
    val exercises: List<ExerciseJsonModel>
)