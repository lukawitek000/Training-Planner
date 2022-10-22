package com.lukasz.witkowski.training.planner.training.domain

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise

/**
 * Class describing exercise in the training.
 * I contains a [Exercise] snapshot and saves it to database to avoid mess with updating and deleting [Exercise]
 */
data class TrainingExercise(
    val id: TrainingExerciseId,
    val exercise: Exercise,
    val repetitions: Int = 1,
    val sets: Int = 1,
    val time: Time = Time.NONE,
    val restTime: Time = Time.NONE
)
