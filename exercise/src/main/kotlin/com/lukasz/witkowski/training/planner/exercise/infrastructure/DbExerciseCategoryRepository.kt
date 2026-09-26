package com.lukasz.witkowski.training.planner.exercise.infrastructure

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class DbExerciseCategoryRepository(
    private val categoryDao: ExerciseCategoryDao,
): ExerciseCategoryRepository {
    override fun getAll(): Flow<List<ExerciseCategory>> = categoryDao.getAll().map {
        it.map { category -> category.toExerciseCategory() }
    }
}