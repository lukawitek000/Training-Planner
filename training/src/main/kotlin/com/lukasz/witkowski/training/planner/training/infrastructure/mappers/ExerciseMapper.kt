package com.lukasz.witkowski.training.planner.training.infrastructure.mappers

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategoryLegacy
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbExerciseSnapshot
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

private fun toDbExercise(exercise: Exercise): DbExerciseSnapshot =
    DbExerciseSnapshot(
        exercise.id.toString(),
        exercise.name,
        exercise.description,
        exercise.categories.first().ordinal,
        null,
    )

private fun toExercise(dbExerciseSnapshot: DbExerciseSnapshot): Exercise =
    Exercise(
        ExerciseId(dbExerciseSnapshot.exerciseId),
        dbExerciseSnapshot.name,
        dbExerciseSnapshot.description,
        listOf(ExerciseCategoryLegacy.entries[dbExerciseSnapshot.category]),
    )
