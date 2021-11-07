package com.lukasz.witkowski.shared.models

data class TrainingExercise(
    val id: Long = 0L,
    val exercise: Exercise,
    val repetitions: Int = 1,
    val sets: Int = 1,
    val time: Long = 0L,
    val restTime: Long = 0L // Rest time after exercise
)
