package com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.mappers

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.models.ExerciseJsonModel

object ExerciseMapper {

    fun toExerciseJsonModel(exercise: TrainingExercise): ExerciseJsonModel {
        return ExerciseJsonModel(
            id = exercise.id.value,
            name = exercise.name,
            description = exercise.description,
            category = exercise.category.ordinal,
            repetitions = exercise.repetitions,
            sets = exercise.sets,
            time = exercise.time.timeInMillis,
            restTime = exercise.restTime.timeInMillis
        )
    }

    fun toExercise(exerciseJsonModel: ExerciseJsonModel): TrainingExercise {
        return TrainingExercise(
            id = TrainingExerciseId(exerciseJsonModel.id),
            name = exerciseJsonModel.name,
            description = exerciseJsonModel.description,
            category = ExerciseCategory.values()[exerciseJsonModel.category],
            image = null,
            repetitions = exerciseJsonModel.repetitions,
            sets = exerciseJsonModel.sets,
            time = Time(exerciseJsonModel.time),
            restTime = Time(exerciseJsonModel.restTime)
        )
    }

}