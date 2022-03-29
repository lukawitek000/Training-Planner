package com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.mappers

import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi.TrainingPlanJsonModel

object TrainingPlanMapper {

    fun toTrainingPlan(trainingPlanJsonModel: TrainingPlanJsonModel): TrainingPlan {
        return TrainingPlan(
            id = trainingPlanJsonModel.id,
            title = trainingPlanJsonModel.title,
            description = trainingPlanJsonModel.description,
            exercises = trainingPlanJsonModel.exercises.map { ExerciseMapper.toExercise(it) }
        )
    }

    fun toTrainingPlanJsonModel(trainingPlan: TrainingPlan): TrainingPlanJsonModel {
        return TrainingPlanJsonModel(
            id = trainingPlan.id,
            title = trainingPlan.title,
            description = trainingPlan.description,
            exercises = trainingPlan.exercises.map { ExerciseMapper.toExerciseJsonModel(it) }
        )
    }
}