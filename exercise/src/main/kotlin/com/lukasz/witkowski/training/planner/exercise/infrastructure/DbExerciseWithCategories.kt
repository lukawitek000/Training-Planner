package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.room3.Embedded
import androidx.room3.Junction
import androidx.room3.Relation

data class DbExerciseWithCategories(
    @Embedded val exercise: DbExercise,
    @Relation(
        parentColumns = ["exerciseId"],
        entityColumns = ["categoryName"],
        associateBy = Junction(ExerciseCategoryCrossRef::class),
    )
    val categories: List<DbExerciseCategory>,
)
