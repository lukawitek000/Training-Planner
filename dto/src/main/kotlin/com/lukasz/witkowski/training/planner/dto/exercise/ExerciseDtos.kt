package com.lukasz.witkowski.training.planner.dto.exercise

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(val id: String, val name: String)

@Serializable
data class OwnerDto(val id: String, val username: String)

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
    val description: String = "",
    val categories: List<CategoryDto> = emptyList(),
    val owner: OwnerDto? = null,
    val source: String = "LOCAL_ONLY",
    val sharing: String = "PRIVATE",
    val revision: Long = 1L,
    val recommendations: List<ExerciseRecommendationDto>? = null,
    val images: List<String> = emptyList()
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
    val description: String = "",
    val categoryIds: List<String> = emptyList(),
    val sharing: String = "PRIVATE",
    val recommendations: List<ExerciseRecommendationDto>? = null,
    val id: String? = null,
    val idempotencyKey: String? = null,
    val images: List<String> = emptyList()
)

@Serializable
data class UpdateExerciseRequestDto(
    val name: String,
    val description: String = "",
    val categoryIds: List<String> = emptyList(),
    val sharing: String = "PRIVATE",
    val expectedVersion: Long? = null,
    val idempotencyKey: String? = null,
    val recommendations: List<ExerciseRecommendationDto>? = null,
    val images: List<String> = emptyList()
)

@Serializable
data class ExerciseConflictDto(val message: String, val currentServerExercise: ExerciseDto)
