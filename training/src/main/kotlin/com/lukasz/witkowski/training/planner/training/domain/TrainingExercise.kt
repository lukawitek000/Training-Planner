package com.lukasz.witkowski.training.planner.training.domain

import com.lukasz.witkowski.training.planner.shared.utils.Weight
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
    val weight: Weight? = null,
    val time: Duration = Duration.ZERO
) {
    val isIsometric: Boolean
        get() = time > Duration.ZERO
}
