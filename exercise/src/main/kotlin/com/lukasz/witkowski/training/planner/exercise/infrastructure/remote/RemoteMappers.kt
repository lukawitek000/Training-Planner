package com.lukasz.witkowski.training.planner.exercise.infrastructure.remote

import com.lukasz.witkowski.training.planner.dto.exercise.CategoryDto
import com.lukasz.witkowski.training.planner.dto.exercise.CreateExerciseRequestDto
import com.lukasz.witkowski.training.planner.dto.exercise.ExerciseDto
import com.lukasz.witkowski.training.planner.dto.exercise.UpdateExerciseRequestDto
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise2
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseOrigin
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseOwner

internal fun CategoryDto.toExerciseCategory(): ExerciseCategory = ExerciseCategory(name = name)

internal fun ExerciseDto.toExercise2(currentUserId: String? = null): Exercise2 {
    val origin = when {
        owner == null -> ExerciseOrigin.Bundled
        owner!!.id == currentUserId -> ExerciseOrigin.UserCreated(
            owner = ExerciseOwner.User(id = owner!!.id, username = owner!!.username)
        )
        else -> ExerciseOrigin.Community(
            owner = ExerciseOwner.User(id = owner!!.id, username = owner!!.username)
        )
    }
    val categoriesSet = categories.map { it.toExerciseCategory() }.toSet()
    return Exercise2(
        id = ExerciseId(id),
        name = name,
        description = description,
        categories = if (categoriesSet.isEmpty()) setOf(ExerciseCategory("Other")) else categoriesSet,
        imageId = null,
        origin = origin
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
