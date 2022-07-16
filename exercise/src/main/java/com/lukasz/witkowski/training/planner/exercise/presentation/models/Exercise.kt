package com.lukasz.witkowski.training.planner.exercise.presentation.models

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ImageId

data class Exercise(
    val id: ExerciseId,
    val name: String,
    val description: String,
    val category: Category,
    val imageId: ImageId? // TODO where to put bitmap, do the another object with id and bitmap?, then pass both exercise and this object to composable??
)
