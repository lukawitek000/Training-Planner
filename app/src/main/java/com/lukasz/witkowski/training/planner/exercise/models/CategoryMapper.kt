package com.lukasz.witkowski.training.planner.exercise.models

import com.lukasz.witkowski.training.planner.exercise.R
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory

internal object CategoryMapper {

    val allCategories = ExerciseCategory.values().map { toPresentationCategory(it) }

    fun toPresentationCategory(exerciseCategory: ExerciseCategory) = when (exerciseCategory) {
        ExerciseCategory.NONE -> Category(exerciseCategory.ordinal, R.string.category_none)
        ExerciseCategory.LEGS -> Category(exerciseCategory.ordinal, R.string.category_legs)
        ExerciseCategory.SHOULDERS -> Category(
            exerciseCategory.ordinal,
            R.string.category_shoulders
        )
        ExerciseCategory.BICEPS -> Category(exerciseCategory.ordinal, R.string.category_biceps)
        ExerciseCategory.TRICEPS -> Category(exerciseCategory.ordinal, R.string.category_triceps)
        ExerciseCategory.CARDIO -> Category(exerciseCategory.ordinal, R.string.category_cardio)
        ExerciseCategory.BACK -> Category(exerciseCategory.ordinal, R.string.category_back)
        ExerciseCategory.ABS -> Category(exerciseCategory.ordinal, R.string.category_abs)
        ExerciseCategory.STRETCHING -> Category(
            exerciseCategory.ordinal,
            R.string.category_stretching
        )
        ExerciseCategory.CHEST -> Category(exerciseCategory.ordinal, R.string.category_chest)
    }

    fun toDomainCategory(category: Category): ExerciseCategory =
        ExerciseCategory.values()[category.id]
}