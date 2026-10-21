package com.lukasz.witkowski.training.planner.exercise.infrastructure.local

import com.lukasz.witkowski.training.planner.exercise.infrastructure.DbExerciseCategory
import kotlinx.coroutines.flow.Flow

internal interface ExerciseCategoryLocalDataSource {
    fun getAllCategories(): Flow<List<DbExerciseCategory>>

    suspend fun insertCategories(categories: List<DbExerciseCategory>)
}
