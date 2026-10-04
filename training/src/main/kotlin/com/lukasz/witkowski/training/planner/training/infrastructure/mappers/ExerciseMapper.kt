package com.lukasz.witkowski.training.planner.training.infrastructure.mappers

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategoryLegacy
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbExercise
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbTrainingExercise

internal fun TrainingExercise.toDbTrainingExercise(trainingId: TrainingPlanId): DbTrainingExercise =
    DbTrainingExercise(
        id = id.toString(),
        trainingId = trainingId.toString(),
        exercise = toDbExercise(exercise),
        repetitions = repetitions,
        sets = sets,
        time = time.timeInMillis,
        restTime = restTime.timeInMillis,
    )

internal fun DbTrainingExercise.toTrainingExercise(): TrainingExercise =
    TrainingExercise(
        id = TrainingExerciseId(id),
        exercise = toExercise(exercise),
        repetitions = repetitions,
        sets = sets,
        time = Time(time),
        restTime = Time(restTime),
    )

private fun toDbExercise(exercise: Exercise): DbExercise =
    DbExercise(
        exercise.id.toString(),
        exercise.name,
        exercise.description,
        exercise.categories.first().ordinal,
        null,
    )

private fun toExercise(dbExercise: DbExercise): Exercise =
    Exercise(
        ExerciseId(dbExercise.exerciseId),
        dbExercise.name,
        dbExercise.description,
        listOf(ExerciseCategoryLegacy.entries[dbExercise.category]),
    )
