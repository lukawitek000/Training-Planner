package com.lukasz.witkowski.training.planner.exercise.presentation.models

import com.lukasz.witkowski.training.planner.image.ImageReference

data class ExerciseDetails(
    val exercise: ExerciseOverview,
    val recommendations: List<Recommendation>,
    val images: List<ImageReference> = emptyList()
)
