package com.lukasz.witkowski.training.planner.exercise.domain

import kotlinx.coroutines.flow.Flow

interface ExerciseCategoryRepository {
    fun getAll() : Flow<List<ExerciseCategory>>
}
