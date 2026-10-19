package com.lukasz.witkowski.training.planner.dto.training

import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseDto
import kotlinx.serialization.Serializable

@Serializable
data class TrainingExerciseDto(
    val id: String,
    val exercise: ExerciseDto,
    val repetitions: Int,
    val sets: Int,
    val restTimeInMillis: Long,
    val weightInKg: Double?
)

@Serializable
data class TrainingPlanOverviewDto(
    val id: String,
    val title: String,
    val description: String,
    val categories: List<String>,
    val exerciseCount: Int,
    val ownerId: String?
)

@Serializable
data class TrainingPlanDto(
    val id: String,
    val title: String,
    val description: String,
    val exercises: List<TrainingExerciseDto>,
    val restTimeInMillis: Long,
    val ownerId: String?
)

@Serializable
data class CreateTrainingExerciseRequestDto(
    val exerciseId: String,
    val repetitions: Int,
    val sets: Int,
    val restTimeInMillis: Long,
    val weightInKg: Double?
)

@Serializable
data class CreateTrainingPlanRequestDto(
    val title: String,
    val description: String,
    val exercises: List<CreateTrainingExerciseRequestDto>,
    val restTimeInMillis: Long
)

@Serializable
data class UpdateTrainingPlanRequestDto(
    val title: String,
    val description: String,
    val exercises: List<CreateTrainingExerciseRequestDto>,
    val restTimeInMillis: Long
)
