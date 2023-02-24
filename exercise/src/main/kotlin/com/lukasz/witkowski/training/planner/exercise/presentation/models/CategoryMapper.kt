package com.lukasz.witkowski.training.planner.exercise.presentation.models

import com.lukasz.witkowski.training.planner.exercise.R
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory

object CategoryMapper {

    fun toExerciseCategory(category: Category): ExerciseCategory =
        ExerciseCategory.values()[category.id]
}

fun ExerciseCategory.toCategory() = when (this) {
    ExerciseCategory.NONE -> Category(ordinal, R.string.category_none)
    ExerciseCategory.LEGS -> Category(ordinal, R.string.category_legs)
    ExerciseCategory.SHOULDERS -> Category(ordinal, R.string.category_shoulders)
    ExerciseCategory.BICEPS -> Category(ordinal, R.string.category_biceps)
    ExerciseCategory.TRICEPS -> Category(ordinal, R.string.category_triceps)
    ExerciseCategory.CARDIO -> Category(ordinal, R.string.category_cardio)
    ExerciseCategory.BACK -> Category(ordinal, R.string.category_back)
    ExerciseCategory.ABS -> Category(ordinal, R.string.category_abs)
    ExerciseCategory.STRETCHING -> Category(ordinal, R.string.category_stretching)
    ExerciseCategory.CHEST -> Category(ordinal, R.string.category_chest)
}

fun Category.toExerciseCategory(): ExerciseCategory =
    ExerciseCategory.values()[id]
