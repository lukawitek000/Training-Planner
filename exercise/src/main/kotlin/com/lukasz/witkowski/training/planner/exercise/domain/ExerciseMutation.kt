package com.lukasz.witkowski.training.planner.exercise.domain

data class ExerciseMutation(
    val id: String,
    val exerciseId: ExerciseId,
    val idempotencyKey: String,
    val expectedVersion: Long,
    val name: String,
    val description: String,
    val categories: Set<ExerciseCategory>,
    val state: MutationState,
    val beginnerRecommendation: ExerciseRecommendation? = null,
    val intermediateRecommendation: ExerciseRecommendation? = null,
    val advancedRecommendation: ExerciseRecommendation? = null,
    val conflictServerExercise: ExerciseDetails? = null
)

enum class MutationState {
    PENDING,
    IN_PROGRESS,
    CONFLICT,
    ERROR,
    COMPLETED
}
