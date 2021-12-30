package com.lukasz.witkowski.shared.models

data class TrainingStatistics(
    val id: Long = 0L,
    val trainingId: Long,
    var exercisesStatistics: List<ExerciseStatistics>
)