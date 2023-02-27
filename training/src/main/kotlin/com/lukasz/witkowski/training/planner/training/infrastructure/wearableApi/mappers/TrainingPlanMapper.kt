package com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.mappers

import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.models.TrainingPlanJsonModel

internal fun TrainingPlanJsonModel.toTrainingPlan(): TrainingPlan {
    return TrainingPlan(
        id = TrainingPlanId(id),
        title = title,
        description = description,
        exercises = exercises.map { it.toTrainingExercise() }
    )
}

internal fun TrainingPlan.toTrainingPlanJsonModel(): TrainingPlanJsonModel {
    return TrainingPlanJsonModel(
        id = id.toString(),
        title = title,
        description = description,
        exercises = exercises.map { it.toExerciseJsonModel() }
    )
}