package com.lukasz.witkowski.training.planner.training.infrastructure.models

import androidx.room3.Embedded
import androidx.room3.Relation
import kotlin.time.Instant

class DbTrainingOverview(
    @Embedded
    val header: DbTrainingOverviewHeader,
    @Relation(
        entity = DbExerciseCategory::class,
        parentColumns = ["trainingPlanId"],
        entityColumns = ["trainingPlanId"],
    )
    val categories: List<DbExerciseCategory>,
)

class DbTrainingOverviewHeader(
    val trainingPlanId: String,
    val title: String,
    val description: String,
    val lastModification: Instant,
    val lastSession: Instant?,
)
