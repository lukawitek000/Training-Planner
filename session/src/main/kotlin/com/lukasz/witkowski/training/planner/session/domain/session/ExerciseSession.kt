package com.lukasz.witkowski.training.planner.session.domain.session

import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import kotlin.time.Duration

internal class ExerciseSession(
    val exercise: TrainingExercise,
    private val startTime: Time,
    val set: Int
) {
    val restTime: Time
        get() = Time(exercise.restTime.inWholeMilliseconds)

    fun hasRestTime() = exercise.restTime.isPositive()

    fun stop(
        isCompleted: Boolean,
        stopTime: Time,
        actualReps: Int = exercise.repetitions,
        actualWeightInKg: Double? = exercise.weightInKg?.toDouble(),
        rpe: Float? = null,
        overrideDuration: Duration? = null
    ): ExerciseAttemptStatistics {
        val attemptDuration =
            overrideDuration?.let { duration ->
                Time(duration.inWholeMilliseconds)
            } ?: (stopTime - startTime)
        return ExerciseAttemptStatistics(
            trainingExerciseId = exercise.id,
            time = attemptDuration,
            set = set,
            completed = isCompleted,
            actualReps = actualReps,
            actualWeightInKg = actualWeightInKg,
            rpe = rpe
        )
    }
}
