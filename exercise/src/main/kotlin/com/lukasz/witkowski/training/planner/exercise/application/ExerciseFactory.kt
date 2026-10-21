package com.lukasz.witkowski.training.planner.exercise.application

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise2
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseMetadata
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseOwner
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSharing
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSource
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSyncState
import com.lukasz.witkowski.training.planner.image.ImageId

internal object ExerciseFactory {
    fun create(
        exerciseConfiguration: ExerciseConfiguration,
        imageIds: List<ImageId> = emptyList(),
        exerciseId: ExerciseId = ExerciseId.create(),
        metadata: ExerciseMetadata = ExerciseMetadata(
            source = ExerciseSource.UserCreated(ExerciseOwner.Local()),
            sharing = ExerciseSharing.Private,
            revision = 1L,
            syncState = ExerciseSyncState.PENDING_UPLOAD
        )
    ): ExerciseDetails = ExerciseDetails(
        exercise =
        Exercise2(
            id = exerciseId,
            name = exerciseConfiguration.name,
            description = exerciseConfiguration.description,
            categories = exerciseConfiguration.categories.toSet(),
            imageIds = imageIds,
            metadata = metadata
        ),
        beginnerRecommendation = exerciseConfiguration.beginnerRecommendation,
        intermediateRecommendation = exerciseConfiguration.intermediateRecommendation,
        advancedRecommendation = exerciseConfiguration.advancedRecommendation
    )
}
