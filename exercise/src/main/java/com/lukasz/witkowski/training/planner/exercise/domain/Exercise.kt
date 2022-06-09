package com.lukasz.witkowski.training.planner.exercise.domain

data class Exercise(
    val id: ExerciseId,
    val name: String,
    val description: String = "",
    val category: ExerciseCategory = ExerciseCategory.NONE,
    //Adrian: As we discussed - Image can be quite big, so instead of keeping it in DB you can
    //store image as file and here you can just hold the reference to that image
    var image: Image? = null
)
