package com.lukasz.witkowski.training.planner.dto.training

import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseDto
import kotlinx.serialization.Serializable

@Serializable
data class TrainingExerciseDto(
    val id: String,
    val exercise: ExerciseDto,
    val repetitions: Int = 1,
    val sets: Int = 1,
    val restTimeInMillis: Long = 0,
    val weightInKg: Double? = null
)

@Serializable
data class TrainingPlanOverviewDto(
    val id: String,
    val title: String,
    val description: String = "",
    val categories: List<String> = emptyList(),
    val exerciseCount: Int = 0,
    val ownerId: String? = null
)

@Serializable
data class TrainingPlanDto(
    val id: String,
    val title: String,
    val description: String = "",
    val exercises: List<TrainingExerciseDto> = emptyList(),
    val restTimeInMillis: Long = 0,
    val ownerId: String? = null
)

@Serializable
data class CreateTrainingExerciseRequestDto(
    val exerciseId: String,
    val repetitions: Int = 1,
    val sets: Int = 1,
    val restTimeInMillis: Long = 0,
    val weightInKg: Double? = null
)

@Serializable
data class CreateTrainingPlanRequestDto(
    val title: String,
    val description: String = "",
    val exercises: List<CreateTrainingExerciseRequestDto> = emptyList(),
    val restTimeInMillis: Long = 0
)

@Serializable
data class UpdateTrainingPlanRequestDto(
    val title: String,
    val description: String = "",
    val exercises: List<CreateTrainingExerciseRequestDto> = emptyList(),
    val restTimeInMillis: Long = 0
)
