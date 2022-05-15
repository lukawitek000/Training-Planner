package com.lukasz.witkowski.training.planner.training.infrastructure.db.models

import androidx.room.Embedded
import androidx.room.Relation

internal data class DbTrainingPlanWithExercises(
    @Embedded val trainingPlan: DbTrainingPlan,
    @Relation(
        parentColumn = "id",
        entityColumn = "trainingId"
    )
    val exercises: List<DbExercise>
)
