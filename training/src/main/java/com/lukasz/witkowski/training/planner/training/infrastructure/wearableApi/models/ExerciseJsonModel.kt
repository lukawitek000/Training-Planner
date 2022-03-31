package com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.models

data class ExerciseJsonModel(
    val id: String,
    val name: String,
    val description: String,
    val category: Int,
    val repetitions: Int,
    val sets: Int,
    val time: Long,
    val restTime: Long
)