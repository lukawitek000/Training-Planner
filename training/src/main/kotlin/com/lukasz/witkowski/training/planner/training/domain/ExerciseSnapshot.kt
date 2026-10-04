package com.lukasz.witkowski.training.planner.training.domain

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.image.ImageId

/**
 * Class describing exercise in the training.
 * It contains a [Exercise] snapshot and saves it to database to detach plan from exercise.
 */
data class ExerciseSnapshot(
    val id: ExerciseId,
    val name: String,
    val description: String,
    val categories: Set<ExerciseCategoryName>,
    val imageId: ImageId? = null,
) {
    init {
        require(categories.isNotEmpty()) {
            "Exercise should have at least one category"
        }
    }
}
