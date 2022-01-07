package com.lukasz.witkowski.shared.models.statistics

import androidx.room.Embedded
import androidx.room.Relation

data class TrainingStatisticsWithExercisesStatistics(
    @Embedded
    val trainingStatistics: TrainingStatistics,
    @Relation(
        parentColumn = "TrainingIdStatistics",
        entityColumn = "trainingStatisticsId"
    )
    val exercisesStatistics: List<ExerciseStatistics>
)
