package com.lukasz.witkowski.training.planner.dto.exercise

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: String,
    val name: String,
)

@Serializable
data class ExerciseDto(
    val id: String,
    val name: String,
    val description: String = "",
    val categories: List<CategoryDto> = emptyList(),
    val ownerId: String? = null,
)

@Serializable
data class CreateExerciseRequestDto(
    val name: String,
    val description: String = "",
    val categoryIds: List<String> = emptyList(),
)

@Serializable
data class UpdateExerciseRequestDto(
    val name: String,
    val description: String = "",
    val categoryIds: List<String> = emptyList(),
)
