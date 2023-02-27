package com.lukasz.witkowski.training.planner.training.infrastructure.db.mappers

import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.infrastructure.db.models.DbExercise
import com.lukasz.witkowski.training.planner.training.infrastructure.db.models.DbTrainingExercise

internal fun TrainingExercise.toDbTrainingExercise(trainingId: TrainingPlanId): DbTrainingExercise {
    return DbTrainingExercise(
        id = id.toString(),
        trainingId = trainingId.toString(),
        exercise = toDbExercise(exercise),
        repetitions = repetitions,
        sets = sets,
        time = time.timeInMillis,
        restTime = restTime.timeInMillis
    )
}

internal fun DbTrainingExercise.toTrainingExercise(): TrainingExercise {
    return TrainingExercise(
        id = TrainingExerciseId(id),
        exercise = toExercise(exercise),
        repetitions = repetitions,
        sets = sets,
        time = Time(time),
        restTime = Time(restTime)
    )
}

private fun toDbExercise(exercise: Exercise): DbExercise {
    return DbExercise(
        exercise.id.toString(),
        exercise.name,
        exercise.description,
        exercise.category.ordinal,
        null
    )
}

private fun toExercise(dbExercise: DbExercise): Exercise {
    return Exercise(
        ExerciseId(dbExercise.exerciseId),
        dbExercise.name,
        dbExercise.description,
        ExerciseCategory.values()[dbExercise.category],
    )
}
