package com.lukasz.witkowski.training.planner.exercise.presentation

internal object ExerciseMapper {


    fun toDomainExercise(exercise: Exercise): com.lukasz.witkowski.training.planner.exercise.domain.Exercise {
        return com.lukasz.witkowski.training.planner.exercise.domain.Exercise(
            id = exercise.id,
            name = exercise.name,
            description = exercise.description,
            category = CategoryMapper.toDomainCategory(exercise.category),
            image = exercise.image
        )
    }

    fun toPresentationExercise(exercise: com.lukasz.witkowski.training.planner.exercise.domain.Exercise): Exercise {
        return Exercise(
            id = exercise.id,
            name = exercise.name,
            description = exercise.description,
            category = CategoryMapper.toPresentationCategory(exercise.category),
            image = exercise.image
        )
    }
}