package com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.mappers

import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.models.TrainingPlanJsonModel

object TrainingPlanMapper {

    fun toTrainingPlan(trainingPlanJsonModel: TrainingPlanJsonModel): TrainingPlan {
        return TrainingPlan(
            id = TrainingPlanId(trainingPlanJsonModel.id),
            title = trainingPlanJsonModel.title,
            description = trainingPlanJsonModel.description,
            exercises = trainingPlanJsonModel.exercises.map { ExerciseMapper.toExercise(it) }
        )
    }

    fun toTrainingPlanJsonModel(trainingPlan: TrainingPlan): TrainingPlanJsonModel {
        return TrainingPlanJsonModel(
            id = trainingPlan.id.toString(),
            title = trainingPlan.title,
            description = trainingPlan.description,
            exercises = trainingPlan.exercises.map { ExerciseMapper.toExerciseJsonModel(it) }
        )
    }
}
