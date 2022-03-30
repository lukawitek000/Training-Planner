package com.lukasz.witkowski.training.planner.exercise

import android.graphics.Bitmap
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import java.util.UUID

data class Exercise(
    val id: ExerciseId,
    val name: String,
    val description: String,
    val category: Category,
    val image: Bitmap?
)
