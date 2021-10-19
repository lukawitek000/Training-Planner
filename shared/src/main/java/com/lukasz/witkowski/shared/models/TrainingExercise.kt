package com.lukasz.witkowski.shared.models

data class TrainingExercise(
    val id: Long,
    val exercise: Exercise,
    val repetitions: Int,
    val sets: Int,
    val time: Long,
    val restTime: Long
)
