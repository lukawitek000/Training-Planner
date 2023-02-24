package com.lukasz.witkowski.training.planner.training.infrastructure.db.mappers

import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.infrastructure.db.models.DbExercise
import com.lukasz.witkowski.training.planner.training.infrastructure.db.models.DbTrainingExercise

internal object ExerciseMapper {

    fun toDbTrainingExercise(
        trainingExercise: TrainingExercise,
        trainingId: String
    ): DbTrainingExercise {
        return DbTrainingExercise(
            id = trainingExercise.id.toString(),
            trainingId = trainingId,
            exercise = toDbExercise(trainingExercise.exercise),
            repetitions = trainingExercise.repetitions,
            sets = trainingExercise.sets,
            time = trainingExercise.time.timeInMillis,
            restTime = trainingExercise.restTime.timeInMillis
        )
    }

    fun toTrainingExercise(dbTrainingExercise: DbTrainingExercise): TrainingExercise {
        return TrainingExercise(
            id = TrainingExerciseId(dbTrainingExercise.id),
            exercise = toExercise(dbTrainingExercise.exercise),
            repetitions = dbTrainingExercise.repetitions,
            sets = dbTrainingExercise.sets,
            time = Time(dbTrainingExercise.time),
            restTime = Time(dbTrainingExercise.restTime)
        )
    }

    private fun toDbExercise(exercise: Exercise): DbExercise {
        return DbExercise(
            exercise.id.toString(),
            exercise.name,
            exercise.description,
            exercise.category.ordinal,
            null // TODO handle image saving the same for training
        )
    }

    private fun toExercise(dbExercise: DbExercise): Exercise {
        return Exercise(
            ExerciseId(dbExercise.exerciseId),
            dbExercise.name,
            dbExercise.description,
            ExerciseCategory.values()[dbExercise.category],
//            dbExercise.imagePath?.let { Image(ImageId.create(), it) }// TODO handle image saving the same for training
        )
    }
}
