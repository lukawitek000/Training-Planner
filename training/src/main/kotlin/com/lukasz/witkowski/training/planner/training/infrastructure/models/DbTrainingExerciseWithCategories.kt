package com.lukasz.witkowski.training.planner.training.infrastructure.models

import androidx.room3.Embedded
import androidx.room3.Relation

class DbTrainingExerciseWithCategories(
    @Embedded
    val trainingExercise: DbTrainingExercise,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["trainingExerciseId"],
    )
    val categories: List<DbExerciseCategory>,
)
