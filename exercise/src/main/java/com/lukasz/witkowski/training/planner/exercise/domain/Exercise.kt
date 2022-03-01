package com.lukasz.witkowski.training.planner.exercise.domain

import android.graphics.Bitmap
import com.lukasz.witkowski.shared.models.Category
import java.util.*

data class Exercise(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val category: Category = Category.None,
    var image: Bitmap? = null
)