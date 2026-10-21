package com.lukasz.witkowski.training.planner.exercise.application

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise2
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.image.ImageId

internal object ExerciseFactory {
    fun create(
        exerciseConfiguration: ExerciseConfiguration,
        imageIds: List<ImageId> = emptyList(),
        exerciseId: ExerciseId = ExerciseId.create()
    ): ExerciseDetails = ExerciseDetails(
        exercise =
        Exercise2(
            id = exerciseId,
            name = exerciseConfiguration.name,
            description = exerciseConfiguration.description,
            categories = exerciseConfiguration.categories.toSet(),
            imageIds = imageIds
        ),
        beginnerRecommendation = exerciseConfiguration.beginnerRecommendation,
        intermediateRecommendation = exerciseConfiguration.intermediateRecommendation,
        advancedRecommendation = exerciseConfiguration.advancedRecommendation
    )

    fun create(
        exerciseConfiguration: ExerciseConfiguration,
        imageId: ImageId?,
        exerciseId: ExerciseId = ExerciseId.create()
    ): ExerciseDetails = create(
        exerciseConfiguration = exerciseConfiguration,
        imageIds = listOfNotNull(imageId),
        exerciseId = exerciseId
    )
}
