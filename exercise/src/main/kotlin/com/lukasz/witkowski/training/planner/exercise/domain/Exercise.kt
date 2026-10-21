package com.lukasz.witkowski.training.planner.exercise.domain

import com.lukasz.witkowski.training.planner.image.ImageId

data class Exercise(
    val id: ExerciseId,
    val name: String,
    val description: String,
    val categories: List<ExerciseCategoryLegacy>,
    val imageId: ImageId? = null
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
    val imageIds: List<ImageId> = emptyList()
) {
    val imageId: ImageId?
        get() = imageIds.firstOrNull()

    constructor(
        id: ExerciseId,
        name: String,
        description: String,
        categories: Set<ExerciseCategory>,
        imageId: ImageId?
    ) : this(
        id = id,
        name = name,
        description = description,
        categories = categories,
        imageIds = listOfNotNull(imageId)
    )

    init {
        require(categories.isNotEmpty()) {
            "Exercise should have at least one category"
        }
    }
}
