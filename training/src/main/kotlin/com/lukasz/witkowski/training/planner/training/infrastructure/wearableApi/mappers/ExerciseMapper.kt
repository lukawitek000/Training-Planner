package com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.mappers

import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.models.ExerciseJsonModel

object ExerciseMapper {

    fun toExerciseJsonModel(trainingExercise: TrainingExercise): ExerciseJsonModel {
        return ExerciseJsonModel(
            id = trainingExercise.id.toString(),
            exerciseId = trainingExercise.exercise.id.toString(),
            name = trainingExercise.exercise.name,
            description = trainingExercise.exercise.description,
            category = trainingExercise.exercise.category.ordinal,
            repetitions = trainingExercise.repetitions,
            sets = trainingExercise.sets,
            time = trainingExercise.time.timeInMillis,
            restTime = trainingExercise.restTime.timeInMillis
        )
    }

    fun toExercise(exerciseJsonModel: ExerciseJsonModel): TrainingExercise {
        return TrainingExercise(
            id = TrainingExerciseId(exerciseJsonModel.id),
            exercise = Exercise(
                ExerciseId(exerciseJsonModel.exerciseId),
                exerciseJsonModel.name,
                exerciseJsonModel.description,
                ExerciseCategory.values()[exerciseJsonModel.category]
            ),
            repetitions = exerciseJsonModel.repetitions,
            sets = exerciseJsonModel.sets,
            time = Time(exerciseJsonModel.time),
            restTime = Time(exerciseJsonModel.restTime)
        )
    }
}
