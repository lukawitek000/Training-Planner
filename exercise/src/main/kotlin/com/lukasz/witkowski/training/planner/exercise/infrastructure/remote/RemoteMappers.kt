package com.lukasz.witkowski.training.planner.exercise.infrastructure.remote

import com.lukasz.witkowski.training.planner.dto.exercise.CategoryDto
import com.lukasz.witkowski.training.planner.dto.exercise.CreateExerciseRequestDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseDto
import com.lukasz.witkowski.training.planner.dto.exercise.UpdateExerciseRequestDto
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise2
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseMetadata
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseOwner
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSharing
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSource
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSyncState

internal fun CategoryDto.toExerciseCategory(): ExerciseCategory = ExerciseCategory(name = name)

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
        categoryIds = categories.map { it.name }
    )

internal fun Exercise2.toUpdateExerciseRequestDto(): UpdateExerciseRequestDto =
    UpdateExerciseRequestDto(
        name = name,
        description = description,
        categoryIds = categories.map { it.name }
    )
