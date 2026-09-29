package com.lukasz.witkowski.training.planner.exercise.presentation.models

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.image.ImageReference

data class Exercise(
    val id: ExerciseId,
    val name: String,
    val description: String,
    val categories: List<Category>,
    val image: ImageReference? = null,
)

data class Exercise2(
    val id: ExerciseId,
    val name: String,
    val description: String,
    val categories: List<ExerciseCategory>,
    val image: ImageReference? = null,
)
