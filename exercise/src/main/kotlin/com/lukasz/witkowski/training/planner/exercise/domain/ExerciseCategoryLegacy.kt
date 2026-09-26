package com.lukasz.witkowski.training.planner.exercise.domain

enum class ExerciseCategoryLegacy {
    NONE,
    LEGS,
    SHOULDERS,
    BICEPS,
    TRICEPS,
    CARDIO,
    BACK,
    ABS,
    STRETCHING,
    CHEST,
    ;

    fun isNone() = this == NONE
}
