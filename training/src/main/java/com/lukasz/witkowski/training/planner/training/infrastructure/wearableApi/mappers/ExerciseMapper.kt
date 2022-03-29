package com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.mappers

import com.lukasz.witkowski.training.planner.exercise.Category
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.ExerciseJsonModel

object ExerciseMapper {

    fun toExerciseJsonModel(exercise: TrainingExercise): ExerciseJsonModel {
        return ExerciseJsonModel(
            id = exercise.id,
            name = exercise.name,
            description = exercise.description,
            category = exercise.category.id,
            repetitions = exercise.repetitions,
            sets = exercise.sets,
            time = exercise.time,
            restTime = exercise.restTime
        )
    }

    fun toExercise(exerciseJsonModel: ExerciseJsonModel): TrainingExercise {
        return TrainingExercise(
            id = exerciseJsonModel.id,
            name = exerciseJsonModel.name,
            description = exerciseJsonModel.description,
            category = com.lukasz.witkowski.training.planner.exercise.Category(), // TODO Mapping category ??
            image = null,
            repetitions = exerciseJsonModel.repetitions,
            sets = exerciseJsonModel.sets,
            time = exerciseJsonModel.time,
            restTime = exerciseJsonModel.restTime
        )
    }

}