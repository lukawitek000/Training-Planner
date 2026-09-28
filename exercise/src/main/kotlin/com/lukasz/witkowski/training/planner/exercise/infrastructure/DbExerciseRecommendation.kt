package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.room3.Entity

@Entity("exercise_recommendation", primaryKeys = ["exerciseId", "level"])
class DbExerciseRecommendation(
    val exerciseId: String,
    val level: RecommendationLevel,
    val sets: Int,
    val reps: Int,
    val restTimeInSeconds: Long,
    val weightInKg: Int?
)

enum class RecommendationLevel {
    BEGINNER, INTERMEDIATE, ADVANCED
}
