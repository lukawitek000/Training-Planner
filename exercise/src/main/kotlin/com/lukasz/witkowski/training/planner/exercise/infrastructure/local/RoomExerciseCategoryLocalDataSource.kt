package com.lukasz.witkowski.training.planner.exercise.infrastructure.local

import com.lukasz.witkowski.training.planner.exercise.infrastructure.DbExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.infrastructure.ExerciseCategoryDao
import kotlinx.coroutines.flow.Flow

internal class RoomExerciseCategoryLocalDataSource(private val categoryDao: ExerciseCategoryDao) :
    ExerciseCategoryLocalDataSource {

    override fun getAllCategories(): Flow<List<DbExerciseCategory>> = categoryDao.getAll()

    override suspend fun insertCategories(categories: List<DbExerciseCategory>) {
        categoryDao.insert(categories)
    }
}
