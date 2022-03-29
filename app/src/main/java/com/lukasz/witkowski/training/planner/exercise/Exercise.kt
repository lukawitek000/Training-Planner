package com.lukasz.witkowski.training.planner.exercise

import android.graphics.Bitmap
import java.util.UUID

data class Exercise(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val category: Category,
    val image: Bitmap?
)
