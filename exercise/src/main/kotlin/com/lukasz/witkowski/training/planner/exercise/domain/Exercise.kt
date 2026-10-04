package com.lukasz.witkowski.training.planner.exercise.domain

import com.lukasz.witkowski.training.planner.image.ImageId

data class Exercise(
    val id: ExerciseId,
    val name: String,
    val description: String,
    val categories: List<ExerciseCategoryLegacy>,
    val imageId: ImageId? = null,
) {
    init {
        require(categories.isNotEmpty()) {
            "Exercise should have at least one category"
        }
    }
}

data class Exercise2(
    val id: ExerciseId,
    val name: String,
    val description: String,
    val categories: Set<ExerciseCategory>,
    val imageId: ImageId? = null,
) {
    init {
        require(categories.isNotEmpty()) {
            "Exercise should have at least one category"
        }
    }
}
