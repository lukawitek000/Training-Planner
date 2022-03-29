package com.lukasz.witkowski.training.planner.exercise.presentation

import com.lukasz.witkowski.training.planner.exercise.R
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory

data class Category internal constructor(
    val id: Int,
    val res: Int = R.string.category_none
) {
    fun isNone() = res == R.string.category_none

    constructor(): this(id = ExerciseCategory.NONE.ordinal)
}

val allCategories = CategoryMapper.allCategories.filter { !it.isNone() }