package com.lukasz.witkowski.shared.models.statistics

import androidx.room.Embedded
import androidx.room.Relation
import com.lukasz.witkowski.shared.models.TrainingExercise

data class ExerciseWithStatistics(
    @Embedded
    val exercise: TrainingExercise,
    @Relation(
        parentColumn = "TrainingExerciseId",
        entityColumn = "trainingExerciseId"
    )
    val statistics: List<ExerciseStatistics>
)

