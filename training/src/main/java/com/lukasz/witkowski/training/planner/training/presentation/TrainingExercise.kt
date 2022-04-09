package com.lukasz.witkowski.training.planner.training.presentation

import android.graphics.Bitmap
import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId

data class TrainingExercise(
    val id: TrainingExerciseId,
    val name: String = "",
    val description: String = "",
    val category: Category = Category(),
    val image: Bitmap? = null,
    val repetitions: Int = 1,
    val sets: Int = 1,
    val time: Time = Time.NONE,
    val restTime: Time = Time.NONE
)
