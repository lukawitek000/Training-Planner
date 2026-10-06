package com.lukasz.witkowski.training.planner.training.presentation.mappers

import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan

fun TrainingPlan.toDomainTrainingPlan() {
    TODO()
}
// fun TrainingPlan.toDomainTrainingPlan(): DomainTrainingPlan =
//    DomainTrainingPlan(
//        id = id,
//        title = title,
//        description = description,
//        exercises = exercises.map { it.toDomainTrainingExercise() },
//        isSynchronized = isSynchronized,
//    )
//
// fun DomainTrainingPlan.toPresentationTrainingPlan(): TrainingPlan =
//    TrainingPlan(
//        id = id,
//        title = title,
//        description = description,
//        exercises = exercises.map { it.toPresentationTrainingExercise() },
//        isSynchronized = isSynchronized,
//    )
//
// fun List<DomainTrainingPlan>.toPresentationTrainingPlans(): List<TrainingPlan> = map { it.toPresentationTrainingPlan() }
