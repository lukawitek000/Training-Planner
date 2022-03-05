package com.lukasz.witkowski.training.planner.training.domain

import android.graphics.Bitmap
import java.util.*

data class Exercise(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val category: String,
    val image: Bitmap? = null,
    val repetitions: Int = 1,
    val sets: Int = 1,
    val time: Long = 0L,
    val restTime: Long = 0L
)
