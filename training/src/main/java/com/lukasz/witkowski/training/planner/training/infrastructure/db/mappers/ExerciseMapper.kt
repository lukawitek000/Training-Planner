package com.lukasz.witkowski.training.planner.training.infrastructure.db.mappers

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.Image
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.infrastructure.db.models.DbExercise

internal object ExerciseMapper {

    fun toDbExercise(exercise: TrainingExercise, trainingId: String) : DbExercise {
        return DbExercise(
            id = exercise.id.value,
            trainingId = trainingId,
            name = exercise.name,
            description = exercise.description,
            categoryId = exercise.category.ordinal,
            image = exercise.image?.data,
            repetitions = exercise.repetitions,
            sets = exercise.sets,
            time = exercise.time,
            restTime = exercise.restTime
        )
    }

    fun toExercise(dbExercise: DbExercise) : TrainingExercise {
        return TrainingExercise(
            id = TrainingExerciseId(dbExercise.id),
            name = dbExercise.name,
            description = dbExercise.description,
            category = ExerciseCategory.values()[dbExercise.categoryId], 
            image = dbExercise.image?.let { Image(it) },
            repetitions = dbExercise.repetitions,
            sets = dbExercise.sets,
            time = dbExercise.time,
            restTime = dbExercise.restTime
        )
    }
}