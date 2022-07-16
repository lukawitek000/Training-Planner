package com.lukasz.witkowski.training.planner.exercise.domain

// TODO Image -> ImageId
data class Exercise(
    val id: ExerciseId,
    val name: String,
    val description: String = "",
    val category: ExerciseCategory = ExerciseCategory.NONE,
    val imageId: ImageId? = null
)
