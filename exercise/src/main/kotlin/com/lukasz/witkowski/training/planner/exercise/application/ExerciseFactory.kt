package com.lukasz.witkowski.training.planner.exercise.application

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise2
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.image.ImageId

internal object ExerciseFactory {
    fun create(
        exerciseConfiguration: ExerciseConfiguration,
        imageId: ImageId?,
        exerciseId: ExerciseId = ExerciseId.create(),
    ): ExerciseDetails =
        ExerciseDetails(
            exercise =
                Exercise2(
                    exerciseId,
                    exerciseConfiguration.name,
                    exerciseConfiguration.description,
                    exerciseConfiguration.categories,
                    imageId,
                ),
            beginnerRecommendation = exerciseConfiguration.beginnerRecommendation,
            intermediateRecommendation = exerciseConfiguration.intermediateRecommendation,
            advancedRecommendation = exerciseConfiguration.advancedRecommendation,
        )
}
