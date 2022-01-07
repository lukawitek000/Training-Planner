package com.lukasz.witkowski.shared.models

data class TrainingStatistics(
    val id: Long = 0L,
    var trainingId: Long,
    var exercisesStatistics: List<ExerciseStatistics>
)