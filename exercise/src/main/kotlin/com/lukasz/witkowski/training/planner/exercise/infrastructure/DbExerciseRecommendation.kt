package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index

@Entity(
    tableName = "exercise_recommendation",
    primaryKeys = ["exerciseId", "level"],
    foreignKeys = [
        ForeignKey(
            entity = DbExercise::class,
            parentColumns = ["exerciseId"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("exerciseId"),
    ],
)
class DbExerciseRecommendation(
    val exerciseId: String,
    val level: RecommendationLevel,
    val sets: Int,
    val reps: Int,
    val restTimeInSeconds: Long,
    val weightInKg: Int?,
)

enum class RecommendationLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
}
