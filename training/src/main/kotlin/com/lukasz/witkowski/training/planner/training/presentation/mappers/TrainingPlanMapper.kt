package com.lukasz.witkowski.training.planner.training.presentation.mappers

import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan as DomainTrainingPlan

fun TrainingPlan.toDomainTrainingPlan(): DomainTrainingPlan {
    return DomainTrainingPlan(
        id = id,
        title = title,
        description = description,
        exercises = exercises.map { it.toDomainTrainingExercise() },
        isSynchronized = isSynchronized
    )
}

fun DomainTrainingPlan.toPresentationTrainingPlan(): TrainingPlan {
    return TrainingPlan(
        id = id,
        title = title,
        description = description,
        exercises = exercises.map { it.toPresentationTrainingExercise() },
        isSynchronized = isSynchronized
    )
}

fun List<DomainTrainingPlan>.toPresentationTrainingPlans(): List<TrainingPlan> {
    return map { it.toPresentationTrainingPlan() }
}
