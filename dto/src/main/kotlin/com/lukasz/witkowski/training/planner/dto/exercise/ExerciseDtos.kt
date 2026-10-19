package com.lukasz.witkowski.training.planner.dto.exercise

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(val id: String, val name: String)

@Serializable
data class OwnerDto(
    val id: String,
    val username: String
)

@Serializable
data class ExerciseRecommendationDto(
    val level: String,
    val sets: Int,
    val reps: Int,
    val restTimeInMillis: Long,
    val weightInKg: Int?,
    val timeInMillis: Long? = null
)

@Serializable
data class ExerciseDto(
    val id: String,
    val name: String,
    val description: String,
    val categories: List<CategoryDto>,
    val owner: OwnerDto?,
    val source: String,
    val sharing: String,
    val revision: Long,
    val recommendations: List<ExerciseRecommendationDto>?
)

@Serializable
data class ExerciseSyncResponseDto(
    val changes: List<ExerciseDto>,
    val deletedIds: List<String>,
    val currentVersion: Long
)

@Serializable
data class CreateExerciseRequestDto(
    val name: String,
    val description: String,
    val categoryIds: List<String>,
    val sharing: String,
    val recommendations: List<ExerciseRecommendationDto>?,
    val id: String? = null,
    val idempotencyKey: String? = null
)

@Serializable
data class UpdateExerciseRequestDto(
    val name: String,
    val description: String,
    val categoryIds: List<String>,
    val sharing: String,
    val expectedVersion: Long?,
    val idempotencyKey: String?,
    val recommendations: List<ExerciseRecommendationDto>?
)

@Serializable
data class ExerciseConflictDto(
    val message: String,
    val currentServerExercise: ExerciseDto
)
