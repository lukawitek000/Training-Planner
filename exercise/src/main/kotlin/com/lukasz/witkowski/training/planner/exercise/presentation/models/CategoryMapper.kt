package com.lukasz.witkowski.training.planner.exercise.presentation.models

import com.lukasz.witkowski.training.planner.exercise.R
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategoryLegacy

fun ExerciseCategoryLegacy.toCategory() =
    when (this) {
        ExerciseCategoryLegacy.NONE -> Category(ordinal, R.string.category_none)
        ExerciseCategoryLegacy.LEGS -> Category(ordinal, R.string.category_legs)
        ExerciseCategoryLegacy.SHOULDERS -> Category(ordinal, R.string.category_shoulders)
        ExerciseCategoryLegacy.BICEPS -> Category(ordinal, R.string.category_biceps)
        ExerciseCategoryLegacy.TRICEPS -> Category(ordinal, R.string.category_triceps)
        ExerciseCategoryLegacy.CARDIO -> Category(ordinal, R.string.category_cardio)
        ExerciseCategoryLegacy.BACK -> Category(ordinal, R.string.category_back)
        ExerciseCategoryLegacy.ABS -> Category(ordinal, R.string.category_abs)
        ExerciseCategoryLegacy.STRETCHING -> Category(ordinal, R.string.category_stretching)
        ExerciseCategoryLegacy.CHEST -> Category(ordinal, R.string.category_chest)
    }

fun Category.toExerciseCategory(): ExerciseCategoryLegacy = ExerciseCategoryLegacy.values()[id]
