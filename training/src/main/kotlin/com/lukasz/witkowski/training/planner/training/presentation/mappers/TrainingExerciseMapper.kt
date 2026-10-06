package com.lukasz.witkowski.training.planner.training.presentation.mappers

import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise

// TODO implementation will be added later
fun TrainingExercise.toDomainTrainingExercise() {
    TODO()
}
//
// fun TrainingExercise.toDomainTrainingExercise(): DomainTrainingExercise =
//    DomainTrainingExercise(
//        id = id,
//        exercise = toDomainExercise(exercise),
//        repetitions = repetitions,
//        sets = sets,
//        time = time,
//        restTime = restTime,
//    )
//
// fun DomainTrainingExercise.toPresentationTrainingExercise(): TrainingExercise =
//    TrainingExercise(
//        id = id,
//        exercise = toPresentationExercise(exercise),
//        repetitions = repetitions,
//        sets = sets,
//        time = time,
//        restTime = restTime,
//    )
//
// private fun toDomainExercise(exercise: Exercise): DomainExercise =
//    DomainExercise(
//        exercise.id,
//        exercise.name,
//        exercise.description,
//        exercise.categories.map { it.toExerciseCategory() },
//        null,
//    )
//
// private fun toPresentationExercise(exercise: DomainExercise): Exercise =
//    Exercise(
//        exercise.id,
//        exercise.name,
//        exercise.description,
//        exercise.categories.map { it.toCategory() },
//        null,
//    )
