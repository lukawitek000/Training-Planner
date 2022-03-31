package com.lukasz.witkowski.training.planner.training.models

import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId

data class TrainingPlan(
    val id: TrainingPlanId,
    val title: String,
    val description: String = "",
    val exercises: List<TrainingExercise>
)