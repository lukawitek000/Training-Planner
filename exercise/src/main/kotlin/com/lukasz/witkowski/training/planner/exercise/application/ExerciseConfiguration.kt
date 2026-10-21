package com.lukasz.witkowski.training.planner.exercise.application

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRecommendation
import com.lukasz.witkowski.training.planner.image.ImageByteArray

data class ExerciseConfiguration(
    val name: String,
    val description: String,
    val categories: List<ExerciseCategory>,
    val images: List<ImageByteArray> = emptyList(),
    val beginnerRecommendation: ExerciseRecommendation,
    val intermediateRecommendation: ExerciseRecommendation,
    val advancedRecommendation: ExerciseRecommendation
) {
    val image: ImageByteArray?
        get() = images.firstOrNull()

    constructor(
        name: String,
        description: String,
        categories: List<ExerciseCategory>,
        image: ImageByteArray?,
        beginnerRecommendation: ExerciseRecommendation,
        intermediateRecommendation: ExerciseRecommendation,
        advancedRecommendation: ExerciseRecommendation
    ) : this(
        name = name,
        description = description,
        categories = categories,
        images = listOfNotNull(image),
        beginnerRecommendation = beginnerRecommendation,
        intermediateRecommendation = intermediateRecommendation,
        advancedRecommendation = advancedRecommendation
    )
}
