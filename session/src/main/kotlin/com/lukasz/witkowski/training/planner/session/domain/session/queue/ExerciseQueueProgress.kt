package com.lukasz.witkowski.training.planner.session.domain.session.queue

data class ExerciseQueueProgress(
    val remainingExerciseSets: Int,
    val currentExerciseTotalSets: Int,
    val totalExercises: Int,
    val remainingExercises: Int,
    val remainingSetsInWorkout: Int,
    val nextSetQueueProgress: ExerciseQueueProgress?,
    val exerciseSetCounts: List<Int> = emptyList()
) {
    val totalSetsInWorkout: Int
        get() = exerciseSetCounts.sum()
    val currentExerciseSet = currentExerciseTotalSets - remainingExerciseSets
    val exerciseIndex =
        (totalExercises - remainingExercises) +
            (
                1.takeIf {
                    remainingExerciseSets > 0
                } ?: 0
                )
}
