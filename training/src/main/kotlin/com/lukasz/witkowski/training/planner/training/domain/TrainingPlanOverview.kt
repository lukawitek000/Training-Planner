package com.lukasz.witkowski.training.planner.training.domain

import kotlin.time.Instant

data class TrainingPlanOverview(
    val id: TrainingPlanId,
    val title: String,
    val description: String,
    val categories: Set<ExerciseCategoryName>,
    val lastModification: Instant,
    val lastSession: Instant?,
)
