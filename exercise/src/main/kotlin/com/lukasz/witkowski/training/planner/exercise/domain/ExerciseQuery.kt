package com.lukasz.witkowski.training.planner.exercise.domain

data class ExerciseQuery(
    val categories: List<ExerciseCategory>,
    val query: String
)