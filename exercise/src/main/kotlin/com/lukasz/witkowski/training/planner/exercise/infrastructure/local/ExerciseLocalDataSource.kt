package com.lukasz.witkowski.training.planner.exercise.infrastructure.local

import androidx.paging.PagingSource
import com.lukasz.witkowski.training.planner.exercise.infrastructure.DbExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.infrastructure.DbExerciseWithCategories
import kotlinx.coroutines.flow.Flow

internal interface ExerciseLocalDataSource {
    fun getExerciseDetailsById(id: String): Flow<DbExerciseDetails?>

    fun getExercisesWithCategories(
        query: String,
        categoriesNames: List<String> = emptyList()
    ): PagingSource<Int, DbExerciseWithCategories>

    suspend fun insertExerciseDetails(details: DbExerciseDetails): Boolean

    suspend fun updateExerciseDetails(details: DbExerciseDetails): Boolean

    suspend fun deleteExerciseById(id: String): Boolean
}
