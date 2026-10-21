package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index

@Entity(
    tableName = "ExerciseImage",
    primaryKeys = ["exerciseId", "imageId"],
    foreignKeys = [
        ForeignKey(
            entity = DbExercise::class,
            parentColumns = ["exerciseId"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("exerciseId")]
)
data class DbExerciseImage(
    val exerciseId: String,
    val imageId: String,
    val orderIndex: Int = 0
)
