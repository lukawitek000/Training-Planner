package com.lukasz.witkowski.training.planner.exercise.domain

enum class ExerciseCategory {
    // TODO why unknown is wrong, make nullable?
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
