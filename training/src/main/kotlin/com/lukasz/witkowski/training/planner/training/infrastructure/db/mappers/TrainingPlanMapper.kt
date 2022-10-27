package com.lukasz.witkowski.training.planner.training.infrastructure.db.mappers

import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.infrastructure.db.models.DbTrainingPlan
import com.lukasz.witkowski.training.planner.training.infrastructure.db.models.DbTrainingPlanWithExercises

internal object TrainingPlanMapper {
    fun toDbTrainingPlanWithExercises(trainingPlan: TrainingPlan): DbTrainingPlanWithExercises {
        return DbTrainingPlanWithExercises(
            trainingPlan = DbTrainingPlan(
                id = trainingPlan.id.value,
                name = trainingPlan.title,
                description = trainingPlan.description,
                isSynchronized = trainingPlan.isSynchronized
            ),
            exercises = trainingPlan.exercises.map {
                ExerciseMapper.toDbTrainingExercise(
                    trainingExercise = it,
                    trainingId = trainingPlan.id.value
                )
            },
        )
    }

    fun toTrainingPlan(dbTrainingPlanWithExercises: DbTrainingPlanWithExercises): TrainingPlan {
        return TrainingPlan(
            id = TrainingPlanId(dbTrainingPlanWithExercises.trainingPlan.id),
            title = dbTrainingPlanWithExercises.trainingPlan.name,
            description = dbTrainingPlanWithExercises.trainingPlan.description,
            exercises = dbTrainingPlanWithExercises.exercises.map { ExerciseMapper.toTrainingExercise(it) },
            isSynchronized = dbTrainingPlanWithExercises.trainingPlan.isSynchronized
        )
    }
}