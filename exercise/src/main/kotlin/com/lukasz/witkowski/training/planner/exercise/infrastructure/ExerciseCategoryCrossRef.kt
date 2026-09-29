package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index

@Entity(
    primaryKeys = ["exerciseId", "categoryName"],
    foreignKeys = [
        ForeignKey(
            entity = DbExercise::class,
            parentColumns = ["exerciseId"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = DbExerciseCategory::class,
            parentColumns = ["categoryName"],
            childColumns = ["categoryName"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("categoryName"),
        Index("exerciseId"),
    ],
)
data class ExerciseCategoryCrossRef(
    val exerciseId: String,
    val categoryName: String,
)
