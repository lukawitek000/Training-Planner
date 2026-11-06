package com.lukasz.witkowski.training.planner.exercise.domain

import com.lukasz.witkowski.training.planner.image.ImageId

data class ExerciseDetails(
    val exercise: ExerciseOverview,
    val beginnerRecommendation: ExerciseRecommendation,
    val intermediateRecommendation: ExerciseRecommendation,
    val advancedRecommendation: ExerciseRecommendation
) {
    val images: List<ImageId>
        get() = exercise.imageIds
}
