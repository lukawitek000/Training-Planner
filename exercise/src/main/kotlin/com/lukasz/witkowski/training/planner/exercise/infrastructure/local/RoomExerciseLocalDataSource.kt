package com.lukasz.witkowski.training.planner.exercise.infrastructure.local

import androidx.paging.PagingSource
import com.lukasz.witkowski.training.planner.exercise.infrastructure.DbExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.infrastructure.DbExerciseWithCategories
import com.lukasz.witkowski.training.planner.exercise.infrastructure.ExerciseDao
import kotlinx.coroutines.flow.Flow

internal class RoomExerciseLocalDataSource(
    private val exerciseDao: ExerciseDao
) : ExerciseLocalDataSource {

    override fun getExerciseDetailsById(id: String): Flow<DbExerciseDetails?> =
        exerciseDao.getExerciseDetailsById(id)

    override fun getExercisesWithCategories(
        query: String,
        categoriesNames: List<String>
    ): PagingSource<Int, DbExerciseWithCategories> =
        if (categoriesNames.isEmpty()) {
            exerciseDao.getExercisesWithCategories(query)
        } else {
            exerciseDao.getExercisesWithCategories(query, categoriesNames)
        }

    override suspend fun insertExerciseDetails(details: DbExerciseDetails): Boolean =
        exerciseDao.insertExerciseDetails(details)

    override suspend fun updateExerciseDetails(details: DbExerciseDetails): Boolean =
        exerciseDao.update(details) == ONE_ROW

    override suspend fun deleteExerciseById(id: String): Boolean =
        exerciseDao.deleteExerciseById(id) == ONE_ROW

    private companion object {
        const val ONE_ROW = 1
    }
}
