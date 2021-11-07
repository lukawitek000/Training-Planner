package com.lukasz.witkowski.shared.models

data class Training(
    val id: Long = 0L,
    val title: String,
    val description: String = "",
    val exercises: List<TrainingExercise> = emptyList()
)
