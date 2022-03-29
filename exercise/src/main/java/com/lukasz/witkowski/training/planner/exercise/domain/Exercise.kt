package com.lukasz.witkowski.training.planner.exercise.domain

import android.graphics.Bitmap
import java.util.*

data class Exercise(
    val id: String = UUID.randomUUID().toString(), // ExerciseId
    val name: String,
    val description: String = "",
    val category: ExerciseCategory = ExerciseCategory.NONE,
    var image: Image? = null
)