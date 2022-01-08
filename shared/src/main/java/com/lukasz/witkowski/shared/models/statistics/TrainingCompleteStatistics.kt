package com.lukasz.witkowski.shared.models.statistics

import androidx.room.Embedded
import androidx.room.Relation

data class TrainingCompleteStatistics(
    @Embedded
    val trainingStatistics: TrainingStatistics,
    @Relation(
        parentColumn = "TrainingIdStatistics",
        entityColumn = "trainingStatisticsId"
    )
    var exercisesStatistics: List<ExerciseStatistics>
)
