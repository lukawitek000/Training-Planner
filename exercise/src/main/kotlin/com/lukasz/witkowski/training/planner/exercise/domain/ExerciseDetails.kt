package com.lukasz.witkowski.training.planner.exercise.domain

import com.lukasz.witkowski.training.planner.image.ImageId

data class ExerciseDetails(
    val exercise: Exercise2,
    val images: List<ImageId>,
    val beginnerRecommendation: ExerciseRecommendation,
    val intermediateRecommendation: ExerciseRecommendation,
    val advancedRecommendation: ExerciseRecommendation
)
