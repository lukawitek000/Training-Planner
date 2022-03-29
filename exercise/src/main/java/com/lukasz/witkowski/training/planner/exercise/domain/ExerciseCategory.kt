package com.lukasz.witkowski.training.planner.exercise.domain

// Publiczny obiekt
// Presentation layer in app module
enum class ExerciseCategory {
    NONE,
    LEGS,
    SHOULDERS,
    BICEPS,
    TRICEPS,
    CARDIO,
    BACK,
    ABS,
    STRETCHING,
    CHEST
}

fun isCategoryNone(category: ExerciseCategory) = category == ExerciseCategory.NONE