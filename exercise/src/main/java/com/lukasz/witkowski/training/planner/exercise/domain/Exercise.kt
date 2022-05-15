package com.lukasz.witkowski.training.planner.exercise.domain

data class Exercise(
    val id: ExerciseId,
    val name: String,
    val description: String = "",
    val category: ExerciseCategory = ExerciseCategory.NONE,
    var image: Image? = null
)
