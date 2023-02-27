package com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.mappers

import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.models.ExerciseJsonModel

internal fun TrainingExercise.toExerciseJsonModel(): ExerciseJsonModel {
    return ExerciseJsonModel(
        id = id.toString(),
        exerciseId = exercise.id.toString(),
        name = exercise.name,
        description = exercise.description,
        category = exercise.category.ordinal,
        repetitions = repetitions,
        sets = sets,
        time = time.timeInMillis,
        restTime = restTime.timeInMillis
    )
}

internal fun ExerciseJsonModel.toTrainingExercise(): TrainingExercise {
    return TrainingExercise(
        id = TrainingExerciseId(id),
        exercise = Exercise(
            ExerciseId(exerciseId),
            name,
            description,
            ExerciseCategory.values()[category]
        ),
        repetitions = repetitions,
        sets = sets,
        time = Time(time),
        restTime = Time(restTime)
    )
}
