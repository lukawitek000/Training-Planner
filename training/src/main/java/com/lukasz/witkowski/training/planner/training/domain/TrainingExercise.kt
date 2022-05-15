package com.lukasz.witkowski.training.planner.training.domain

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.Image

data class TrainingExercise(
    val id: TrainingExerciseId,
    val name: String = "",
    val description: String = "",
    val category: ExerciseCategory = ExerciseCategory.NONE,
    val image: Image? = null,
    val repetitions: Int = 1,
    val sets: Int = 1,
    val time: Time = Time.NONE,
    val restTime: Time = Time.NONE
)
