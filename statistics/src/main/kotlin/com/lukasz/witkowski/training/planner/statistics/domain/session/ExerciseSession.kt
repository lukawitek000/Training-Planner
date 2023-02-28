package com.lukasz.witkowski.training.planner.statistics.domain.session

import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.statistics.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise

internal class ExerciseSession(
    private val exercise: TrainingExercise,
    private val startTime: Time,
    private val set: Int
) {

    fun hasRestTime() = exercise.restTime.isNotZero()

    fun stop(isCompleted: Boolean, stopTime: Time): ExerciseAttemptStatistics {
        return ExerciseAttemptStatistics(
            trainingExerciseId = exercise.id,
            time = stopTime - startTime,
            set = set,
            completed = isCompleted
        )
    }
}
