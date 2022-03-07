package com.lukasz.witkowski.training.planner.training.infrastructure.db.mappers

import com.lukasz.witkowski.training.planner.training.domain.Exercise
import com.lukasz.witkowski.training.planner.training.infrastructure.db.models.DbExercise

internal object ExerciseMapper {

    fun toDbExercise(exercise: Exercise, trainingId: String) : DbExercise {
        return DbExercise(
            id = exercise.id,
            trainingId = trainingId,
            name = exercise.name,
            description = exercise.description,
            category = exercise.category,
            image = exercise.image,
            repetitions = exercise.repetitions,
            sets = exercise.sets,
            time = exercise.time,
            restTime = exercise.restTime
        )
    }

    fun toExercise(dbExercise: DbExercise) : Exercise {
        return Exercise(
            id = dbExercise.id,
            name = dbExercise.name,
            description = dbExercise.description,
            category = dbExercise.category,
            image = dbExercise.image,
            repetitions = dbExercise.repetitions,
            sets = dbExercise.sets,
            time = dbExercise.time,
            restTime = dbExercise.restTime
        )
    }
}