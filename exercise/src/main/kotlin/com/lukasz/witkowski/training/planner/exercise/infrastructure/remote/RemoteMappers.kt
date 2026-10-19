package com.lukasz.witkowski.training.planner.exercise.infrastructure.remote

import com.lukasz.witkowski.training.planner.dto.exercise.CategoryDto
import com.lukasz.witkowski.training.planner.dto.exercise.CreateExerciseRequestDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseRecommendationDto
import com.lukasz.witkowski.training.planner.dto.exercise.UpdateExerciseRequestDto
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise2
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseMetadata
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseOwner
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSharing
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSource
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSyncState

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRecommendation
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal fun CategoryDto.toExerciseCategory(): ExerciseCategory = ExerciseCategory(name = name)

internal fun ExerciseDto.toExerciseDetails(currentUserId: String? = null): ExerciseDetails {
    val domainExercise = toExercise2(currentUserId)
    val recMap = recommendations?.associateBy { it.level } ?: emptyMap()

    fun getRec(level: String): ExerciseRecommendation {
        val dto = recMap[level]
        return if (dto != null) {
            ExerciseRecommendation(
                sets = dto.sets,
                reps = dto.reps,
                restTime = dto.restTimeInMillis.milliseconds,
                weightInKg = dto.weightInKg
            )
        } else {
            ExerciseRecommendation(sets = 0, reps = 0, restTime = 0.seconds, weightInKg = null)
        }
    }

    return ExerciseDetails(
        exercise = domainExercise,
        beginnerRecommendation = getRec("BEGINNER"),
        intermediateRecommendation = getRec("INTERMEDIATE"),
        advancedRecommendation = getRec("ADVANCED")
    )
}

internal fun ExerciseDto.toExercise2(currentUserId: String? = null): Exercise2 {
    val sourceObj = when (source) {
        "USER_CREATED" -> {
            val ownerObj = if (owner != null) {
                ExerciseOwner.User(id = owner!!.id, username = owner!!.username)
            } else {
                ExerciseOwner.Local()
            }
            ExerciseSource.UserCreated(ownerObj)
        }
        "ORGANIZATION" -> ExerciseSource.Organization
        else -> ExerciseSource.Bundled
    }
    val sharingObj = when (sharing) {
        "COMMUNITY" -> ExerciseSharing.Community
        "FRIENDS" -> ExerciseSharing.Friends()
        else -> ExerciseSharing.Private
    }
    val categoriesSet = categories.map { it.toExerciseCategory() }.toSet()
    val metadataObj = ExerciseMetadata(
        source = sourceObj,
        sharing = sharingObj,
        revision = revision,
        syncState = ExerciseSyncState.SYNCED
    )

    return Exercise2(
        id = ExerciseId(id),
        name = name,
        description = description,
        categories = if (categoriesSet.isEmpty()) setOf(ExerciseCategory("Other")) else categoriesSet,
        imageId = null,
        metadata = metadataObj
    )
}

internal fun Exercise2.toCreateExerciseRequestDto(): CreateExerciseRequestDto =
    CreateExerciseRequestDto(
        name = name,
        description = description,
        categoryIds = categories.map { it.name },
        sharing = when (metadata.sharing) {
            is ExerciseSharing.Community -> "COMMUNITY"
            is ExerciseSharing.Friends -> "FRIENDS"
            else -> "PRIVATE"
        },
        recommendations = null
    )

internal fun Exercise2.toUpdateExerciseRequestDto(
    expectedVersion: Long? = null,
    idempotencyKey: String? = null
): UpdateExerciseRequestDto =
    UpdateExerciseRequestDto(
        name = name,
        description = description,
        categoryIds = categories.map { it.name },
        sharing = when (metadata.sharing) {
            is ExerciseSharing.Community -> "COMMUNITY"
            is ExerciseSharing.Friends -> "FRIENDS"
            else -> "PRIVATE"
        },
        expectedVersion = expectedVersion ?: metadata.revision,
        idempotencyKey = idempotencyKey,
        recommendations = null
    )

internal fun ExerciseDetails.toCreateExerciseRequestDto(): CreateExerciseRequestDto {
    val recList = listOf(
        ExerciseRecommendationDto("BEGINNER", beginnerRecommendation.sets, beginnerRecommendation.reps, beginnerRecommendation.restTime.inWholeMilliseconds, beginnerRecommendation.weightInKg),
        ExerciseRecommendationDto("INTERMEDIATE", intermediateRecommendation.sets, intermediateRecommendation.reps, intermediateRecommendation.restTime.inWholeMilliseconds, intermediateRecommendation.weightInKg),
        ExerciseRecommendationDto("ADVANCED", advancedRecommendation.sets, advancedRecommendation.reps, advancedRecommendation.restTime.inWholeMilliseconds, advancedRecommendation.weightInKg)
    )
    return CreateExerciseRequestDto(
        name = exercise.name,
        description = exercise.description,
        categoryIds = exercise.categories.map { it.name },
        sharing = when (exercise.metadata.sharing) {
            is ExerciseSharing.Community -> "COMMUNITY"
            is ExerciseSharing.Friends -> "FRIENDS"
            else -> "PRIVATE"
        },
        recommendations = recList
    )
}

internal fun ExerciseDetails.toUpdateExerciseRequestDto(
    expectedVersion: Long? = null,
    idempotencyKey: String? = null
): UpdateExerciseRequestDto {
    val recList = listOf(
        ExerciseRecommendationDto("BEGINNER", beginnerRecommendation.sets, beginnerRecommendation.reps, beginnerRecommendation.restTime.inWholeMilliseconds, beginnerRecommendation.weightInKg),
        ExerciseRecommendationDto("INTERMEDIATE", intermediateRecommendation.sets, intermediateRecommendation.reps, intermediateRecommendation.restTime.inWholeMilliseconds, intermediateRecommendation.weightInKg),
        ExerciseRecommendationDto("ADVANCED", advancedRecommendation.sets, advancedRecommendation.reps, advancedRecommendation.restTime.inWholeMilliseconds, advancedRecommendation.weightInKg)
    )
    return UpdateExerciseRequestDto(
        name = exercise.name,
        description = exercise.description,
        categoryIds = exercise.categories.map { it.name },
        sharing = when (exercise.metadata.sharing) {
            is ExerciseSharing.Community -> "COMMUNITY"
            is ExerciseSharing.Friends -> "FRIENDS"
            else -> "PRIVATE"
        },
        expectedVersion = expectedVersion ?: exercise.metadata.revision,
        idempotencyKey = idempotencyKey,
        recommendations = recList
    )
}
