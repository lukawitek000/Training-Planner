package com.lukasz.witkowski.training.planner.exercise.infrastructure

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategoryRepository
import com.lukasz.witkowski.training.planner.exercise.infrastructure.local.ExerciseCategoryLocalDataSource
import com.lukasz.witkowski.training.planner.exercise.infrastructure.remote.ExerciseRemoteDataSource
import com.lukasz.witkowski.training.planner.shared.utils.AppResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class DefaultExerciseCategoryRepository(
    private val localDataSource: ExerciseCategoryLocalDataSource,
    private val remoteDataSource: ExerciseRemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher
) : ExerciseCategoryRepository {

    override fun getAll(): Flow<List<ExerciseCategory>> =
        localDataSource.getAllCategories()
            .onStart {
                syncRemoteCategories()
            }
            .map { list -> list.map { it.toExerciseCategory() } }

    suspend fun syncRemoteCategories() = withContext(ioDispatcher) {
        Timber.d("Syncing remote categories into local storage")
        val result = remoteDataSource.getCategories()
        if (result is AppResult.Success) {
            val dbCategories = result.value.map { DbExerciseCategory(it.name) }
            localDataSource.insertCategories(dbCategories)
        }
    }
}
