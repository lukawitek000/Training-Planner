package com.lukasz.witkowski.training.planner.exercise.domain

data class ExercisePagingConfig(
    val pageSize: Int = 10,
    val prefetchDistance: Int = 10
)

data class ExerciseRepositoryConfig(
    val autoSaveRemoteExercises: Boolean = false,
    val pagingConfig: ExercisePagingConfig = ExercisePagingConfig()
)
