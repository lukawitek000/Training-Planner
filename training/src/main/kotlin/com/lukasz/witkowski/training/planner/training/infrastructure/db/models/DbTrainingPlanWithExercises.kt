package com.lukasz.witkowski.training.planner.training.infrastructure.db.models

import androidx.room3.Embedded
import androidx.room3.Relation

data class DbTrainingPlanWithExercises(
    @Embedded val trainingPlan: DbTrainingPlan,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["trainingId"]
    )
    val exercises: List<DbTrainingExercise>
)
