package com.lukasz.witkowski.training.planner.exercise.presentation.models

import com.lukasz.witkowski.training.planner.exercise.R
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory

data class Category constructor(
    val id: Int,
    val res: Int = R.string.category_none
) {
    fun isNone() = res == R.string.category_none

    constructor(): this(id = ExerciseCategory.NONE.ordinal)
}
