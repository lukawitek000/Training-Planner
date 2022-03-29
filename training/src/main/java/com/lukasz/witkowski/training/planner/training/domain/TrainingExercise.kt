package com.lukasz.witkowski.training.planner.training.domain

import android.graphics.Bitmap
import com.lukasz.witkowski.training.planner.exercise.Category
import java.util.*

data class TrainingExercise(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val category: com.lukasz.witkowski.training.planner.exercise.Category = com.lukasz.witkowski.training.planner.exercise.Category(), // TODO Using Category from presentation layer form exercise (android res)
    val image: Bitmap? = null,
    val repetitions: Int = 1,
    val sets: Int = 1,
    val time: Long = 0L,
    val restTime: Long = 0L
)
