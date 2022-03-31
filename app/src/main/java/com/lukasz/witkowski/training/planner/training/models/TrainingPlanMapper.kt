package com.lukasz.witkowski.training.planner.training.models

object TrainingPlanMapper {

    fun toDomainTrainingPlan(trainingPlan: TrainingPlan): com.lukasz.witkowski.training.planner.training.domain.TrainingPlan {
        return com.lukasz.witkowski.training.planner.training.domain.TrainingPlan(
            id = trainingPlan.id,
            title = trainingPlan.title,
            description = trainingPlan.description,
            exercises = trainingPlan.exercises.map {
                TrainingExerciseMapper.toDomainTrainingExercise(
                    it
                )
            },
            isSynchronized = trainingPlan.isSynchronized
        )
    }

    fun toPresentationTrainingPlan(trainingPlan: com.lukasz.witkowski.training.planner.training.domain.TrainingPlan): TrainingPlan {
        return TrainingPlan(
            id = trainingPlan.id,
            title = trainingPlan.title,
            description = trainingPlan.description,
            exercises = trainingPlan.exercises.map {
                TrainingExerciseMapper.toPresentationTrainingExercise(
                    it
                )
            },
            isSynchronized = trainingPlan.isSynchronized
        )
    }

    fun toPresentationTrainingPlans(trainingPlans: List<com.lukasz.witkowski.training.planner.training.domain.TrainingPlan>): List<TrainingPlan> {
        return trainingPlans.map { toPresentationTrainingPlan(it) }
    }
}