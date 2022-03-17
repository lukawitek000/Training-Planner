package com.lukasz.witkowski.training.planner.exercise.presentation

import com.lukasz.witkowski.training.planner.exercise.R
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory

internal object CategoryMapper {

    val allCategories = ExerciseCategory.values().map { toPresentationCategory(it) }

    fun toPresentationCategory(exerciseCategory: ExerciseCategory) = when (exerciseCategory) {
        ExerciseCategory.NONE -> Category(R.string.category_none)
        ExerciseCategory.LEGS -> Category(R.string.category_legs)
        ExerciseCategory.SHOULDERS -> Category(R.string.category_shoulders)
        ExerciseCategory.BICEPS -> Category(R.string.category_biceps)
        ExerciseCategory.TRICEPS -> Category(R.string.category_triceps)
        ExerciseCategory.CARDIO -> Category(R.string.category_cardio)
        ExerciseCategory.BACK -> Category(R.string.category_back)
        ExerciseCategory.ABS -> Category(R.string.category_abs)
        ExerciseCategory.STRETCHING -> Category(R.string.category_stretching)
        ExerciseCategory.CHEST -> Category(R.string.category_chest)
    }

    fun toDomainCategory(category: Category): ExerciseCategory =
        when (category.res) {
            R.string.category_none -> ExerciseCategory.NONE
            R.string.category_legs -> ExerciseCategory.LEGS
            R.string.category_shoulders -> ExerciseCategory.SHOULDERS
            R.string.category_biceps -> ExerciseCategory.BICEPS
            R.string.category_triceps -> ExerciseCategory.TRICEPS
            R.string.category_cardio -> ExerciseCategory.CARDIO
            R.string.category_back -> ExerciseCategory.BACK
            R.string.category_abs -> ExerciseCategory.ABS
            R.string.category_stretching -> ExerciseCategory.STRETCHING
            R.string.category_chest -> ExerciseCategory.CHEST
            else -> ExerciseCategory.NONE
        }
}