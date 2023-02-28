package com.lukasz.witkowski.training.planner.training.presentation.mappers

import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise
import com.lukasz.witkowski.training.planner.exercise.presentation.models.toCategory
import com.lukasz.witkowski.training.planner.exercise.presentation.models.toExerciseCategory
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingExercise
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise as DomainExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise as DomainTrainingExercise

fun TrainingExercise.toDomainTrainingExercise(): DomainTrainingExercise {
    return DomainTrainingExercise(
        id = id,
        exercise = toDomainExercise(exercise),
        repetitions = repetitions,
        sets = sets,
        time = time,
        restTime = restTime
    )
}

fun DomainTrainingExercise.toPresentationTrainingExercise(): TrainingExercise {
    return TrainingExercise(
        id = id,
        exercise = toPresentationExercise(exercise),
        repetitions = repetitions,
        sets = sets,
        time = time,
        restTime = restTime
    )
}

private fun toDomainExercise(exercise: Exercise): DomainExercise {
    return DomainExercise(
        exercise.id,
        exercise.name,
        exercise.description,
        exercise.category.toExerciseCategory(),
        null
    )
}

private fun toPresentationExercise(exercise: DomainExercise): Exercise {
    return Exercise(
        exercise.id, exercise.name, exercise.description, exercise.category.toCategory(), null
    )
}
