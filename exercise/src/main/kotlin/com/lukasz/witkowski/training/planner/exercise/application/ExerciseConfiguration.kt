package com.lukasz.witkowski.training.planner.exercise.application

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategoryLegacy
import com.lukasz.witkowski.training.planner.image.ImageByteArray

data class ExerciseConfiguration(
    val name: String,
    val description: String,
    val category: ExerciseCategoryLegacy,
    val image: ImageByteArray? = null,
)
