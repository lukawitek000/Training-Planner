package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.room.Embedded
import androidx.room.Relation

internal data class ExerciseWithImage(
    @Embedded val exercise: DbExercise,
    @Relation(
        parentColumn = "id",
        entityColumn = "exerciseId"
    )
    val imageReference: DbImageReference?
)
