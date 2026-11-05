package com.lukasz.witkowski.training.planner.exercise.presentation.models

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseMetadata
import com.lukasz.witkowski.training.planner.image.ImageReference

data class ExerciseOverview(
    val id: ExerciseId,
    val name: String,
    val description: String,
    val categories: List<ExerciseCategory>,
    val thumbnail: ImageReference? = null,
    val metadata: ExerciseMetadata = ExerciseMetadata()
) {
    val image: ImageReference?
        get() = thumbnail
}

typealias Exercise = ExerciseOverview

@Deprecated("Use ExerciseOverview instead", ReplaceWith("ExerciseOverview"))
typealias Exercise2 = ExerciseOverview
