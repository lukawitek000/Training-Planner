package com.lukasz.witkowski.training.planner.exercise.models

import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory

data class Category internal constructor(
    val id: Int,
    val res: Int = R.string.category_none
) {
    fun isNone() = res == R.string.category_none

    constructor(): this(id = ExerciseCategory.NONE.ordinal)
}
