package com.lukasz.witkowski.training.planner.training.infrastructure.db.mappers

import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.infrastructure.db.models.DbTrainingPlan
import com.lukasz.witkowski.training.planner.training.infrastructure.db.models.DbTrainingPlanWithExercises

internal object TrainingPlanMapper {
    fun toDbTrainingPlanWithExercises(trainingPlan: TrainingPlan): DbTrainingPlanWithExercises {
        return DbTrainingPlanWithExercises(
            trainingPlan = DbTrainingPlan(
                id = trainingPlan.id,
                name = trainingPlan.title,
                description = trainingPlan.description,
                isSynchronized = trainingPlan.isSynchronized
            ),
            exercises = trainingPlan.exercises.map { ExerciseMapper.toDbExercise(exercise = it, trainingId = trainingPlan.id) },
        )
    }

    fun toTrainingPlan(dbTrainingPlanWithExercises: DbTrainingPlanWithExercises): TrainingPlan {
        return TrainingPlan(
            id = dbTrainingPlanWithExercises.trainingPlan.id,
            title = dbTrainingPlanWithExercises.trainingPlan.name,
            description = dbTrainingPlanWithExercises.trainingPlan.description,
            exercises = dbTrainingPlanWithExercises.exercises.map { ExerciseMapper.toExercise(it) },
            isSynchronized = dbTrainingPlanWithExercises.trainingPlan.isSynchronized
        )
    }
}