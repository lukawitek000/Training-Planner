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
data class ExerciseDto(
    val id: String,
    val name: String,
    val description: String = "",
    val categories: List<CategoryDto> = emptyList(),
    val owner: OwnerDto? = null,
    val source: String = "BUNDLED",
    val sharing: String = "PRIVATE",
    val revision: Long = 1L
)

@Serializable
data class ExerciseSyncResponseDto(
    val changes: List<ExerciseDto> = emptyList(),
    val deletedIds: List<String> = emptyList(),
    val currentVersion: Long = 0L
)

@Serializable
data class CreateExerciseRequestDto(
    val name: String,
    val description: String = "",
    val categoryIds: List<String> = emptyList(),
    val sharing: String = "PRIVATE"
)

@Serializable
data class UpdateExerciseRequestDto(
    val name: String,
    val description: String = "",
    val categoryIds: List<String> = emptyList(),
    val sharing: String = "PRIVATE"
)
