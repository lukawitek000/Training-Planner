package com.lukasz.witkowski.training.planner.training.domain

data class TrainingPlanOverview(
    val id: TrainingPlanId,
    val title: String,
    val description: String = "",
    val categories: Set<ExerciseCategoryName>
)
