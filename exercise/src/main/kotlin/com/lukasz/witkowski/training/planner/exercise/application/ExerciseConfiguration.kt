package com.lukasz.witkowski.training.planner.exercise.application

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategoryLegacy
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRecommendation
import com.lukasz.witkowski.training.planner.image.ImageByteArray

data class ExerciseConfiguration(
    val name: String,
    val description: String,
    val categories: List<ExerciseCategory>,
    val image: ImageByteArray? = null,
    val beginnerRecommendation: ExerciseRecommendation,
    val intermediateRecommendation: ExerciseRecommendation,
    val advancedRecommendation: ExerciseRecommendation,
)
