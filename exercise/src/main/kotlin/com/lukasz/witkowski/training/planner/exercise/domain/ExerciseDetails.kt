package com.lukasz.witkowski.training.planner.exercise.domain

data class ExerciseDetails(
    val exercise: Exercise,
    val beginnerRecommendation: ExerciseRecommendation,
    val intermediateRecommendation: ExerciseRecommendation,
    val advancedRecommendation: ExerciseRecommendation
)
