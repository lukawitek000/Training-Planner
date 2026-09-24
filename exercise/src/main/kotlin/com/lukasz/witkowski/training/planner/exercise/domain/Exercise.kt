package com.lukasz.witkowski.training.planner.exercise.domain

import com.lukasz.witkowski.training.planner.image.ImageId

data class Exercise(
    val id: ExerciseId,
    val name: String,
    val description: String,
    val categories: List<ExerciseCategory>,
    val imageId: ImageId? = null,
)
