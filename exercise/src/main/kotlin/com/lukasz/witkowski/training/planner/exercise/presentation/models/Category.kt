package com.lukasz.witkowski.training.planner.exercise.presentation.models

import android.os.Parcelable
import com.lukasz.witkowski.training.planner.exercise.R
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category constructor(
    val id: Int,
    val res: Int = R.string.category_none
): Parcelable {
    constructor() : this(id = ExerciseCategory.NONE.ordinal, res = R.string.category_none)

    fun isNone() = id == ExerciseCategory.NONE.ordinal
}
