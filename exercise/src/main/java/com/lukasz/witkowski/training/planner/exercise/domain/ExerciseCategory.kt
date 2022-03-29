package com.lukasz.witkowski.training.planner.exercise.domain

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
// TODO How to check category None?
fun isCategoryNone(category: ExerciseCategory) = category == ExerciseCategory.NONE
