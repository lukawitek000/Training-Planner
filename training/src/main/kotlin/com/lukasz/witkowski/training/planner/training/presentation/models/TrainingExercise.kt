package com.lukasz.witkowski.training.planner.training.presentation.models

import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId

data class TrainingExercise(
    val id: TrainingExerciseId,
    val exercise: Exercise,
    val repetitions: Int = 1,
    val sets: Int = 1,
    val time: Time = Time.ZERO,
    val restTime: Time = Time.ZERO
)
