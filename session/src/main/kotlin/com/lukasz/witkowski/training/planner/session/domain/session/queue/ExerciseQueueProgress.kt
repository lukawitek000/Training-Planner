package com.lukasz.witkowski.training.planner.session.domain.session.queue

data class ExerciseQueueProgress(
    val remainingExerciseSets: Int,
    val currentExerciseTotalSets: Int,
    val totalExercises: Int,
    val remainingExercises: Int,
    val remainingSetsInWorkout: Int,
    val nextSetQueueProgress: ExerciseQueueProgress?
) {
    val exerciseIndex = totalExercises - remainingExercises + 1
    val currentExerciseSet = currentExerciseTotalSets - remainingExerciseSets
}
