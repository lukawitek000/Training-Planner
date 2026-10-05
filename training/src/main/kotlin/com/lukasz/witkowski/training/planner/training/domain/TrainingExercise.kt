package com.lukasz.witkowski.training.planner.training.domain

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import kotlin.time.Duration

/**
 * Class describing exercise in the training.
 * It contains a [Exercise] snapshot and saves it to database to detach plan from exercise.
 */
data class TrainingExercise(
    val id: TrainingExerciseId,
    val exercise: ExerciseSnapshot,
    val repetitions: Int = 1,
    val sets: Int = 1,
    val restTime: Duration = Duration.ZERO,
    val weightInKg: Int? = null,
)

