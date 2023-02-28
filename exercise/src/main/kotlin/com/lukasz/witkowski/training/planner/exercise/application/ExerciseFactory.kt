package com.lukasz.witkowski.training.planner.exercise.application

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.image.ImageId

internal object ExerciseFactory {
    fun create(
        exerciseConfiguration: ExerciseConfiguration,
        imageId: ImageId?,
        exerciseId: ExerciseId = ExerciseId.create()
    ): Exercise {
        return Exercise(
            exerciseId,
            exerciseConfiguration.name,
            exerciseConfiguration.description,
            exerciseConfiguration.category,
            imageId
        )
    }
}
