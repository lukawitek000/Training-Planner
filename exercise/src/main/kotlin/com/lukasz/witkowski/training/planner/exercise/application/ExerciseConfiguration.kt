package com.lukasz.witkowski.training.planner.exercise.application

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.image.Image
import com.lukasz.witkowski.training.planner.image.ImageByteArray

data class ExerciseConfiguration(
    val name: String,
    val description: String,
    val category: ExerciseCategory,
    val image: Image? = null
)
