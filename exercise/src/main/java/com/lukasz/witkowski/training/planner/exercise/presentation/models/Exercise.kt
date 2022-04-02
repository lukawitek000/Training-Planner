package com.lukasz.witkowski.training.planner.exercise.presentation.models

import android.graphics.Bitmap
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId

data class Exercise(
    val id: ExerciseId,
    val name: String,
    val description: String,
    val category: Category,
    val image: Bitmap?
)
