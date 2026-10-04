package com.lukasz.witkowski.training.planner.training.infrastructure.mappers

import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbTrainingPlan
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbTrainingPlanWithExercises

internal fun TrainingPlan.toDbTrainingPlanWithExercises(): DbTrainingPlanWithExercises =
    DbTrainingPlanWithExercises(
        trainingPlan =
            DbTrainingPlan(
                id = id.toString(),
                name = title,
                description = description,
                isSynchronized = isSynchronized,
            ),
        exercises = exercises.map { it.toDbTrainingExercise(id) },
    )

internal fun DbTrainingPlanWithExercises.toTrainingPlan(): TrainingPlan =
    TrainingPlan(
        id = TrainingPlanId(trainingPlan.id),
        title = trainingPlan.name,
        description = trainingPlan.description,
        exercises = exercises.map { it.toTrainingExercise() },
        isSynchronized = trainingPlan.isSynchronized,
    )
