package com.lukasz.witkowski.training.planner.training.presentation.mappers

import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise
import com.lukasz.witkowski.training.planner.exercise.presentation.models.toCategory
import com.lukasz.witkowski.training.planner.exercise.presentation.models.toExerciseCategory
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingExercise
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise as DomainExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise as DomainTrainingExercise

object TrainingExerciseMapper {

    fun toDomainTrainingExercise(trainingExercise: TrainingExercise): DomainTrainingExercise {
        return DomainTrainingExercise(
            id = trainingExercise.id,
            exercise = toDomainExercise(trainingExercise.exercise),
            repetitions = trainingExercise.repetitions,
            sets = trainingExercise.sets,
            time = trainingExercise.time,
            restTime = trainingExercise.restTime
        )
    }

    fun toPresentationTrainingExercise(trainingExercise: DomainTrainingExercise): TrainingExercise {
        return TrainingExercise(
            id = trainingExercise.id,
            exercise = toPresentationExercise(trainingExercise.exercise),
            repetitions = trainingExercise.repetitions,
            sets = trainingExercise.sets,
            time = trainingExercise.time,
            restTime = trainingExercise.restTime
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
}
