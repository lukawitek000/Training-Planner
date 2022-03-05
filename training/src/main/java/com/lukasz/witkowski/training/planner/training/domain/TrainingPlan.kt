package com.lukasz.witkowski.training.planner.training.domain

import java.util.*

data class TrainingPlan(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val exercises: List<Exercise>,
    val isSynchronized: Boolean = false
)
