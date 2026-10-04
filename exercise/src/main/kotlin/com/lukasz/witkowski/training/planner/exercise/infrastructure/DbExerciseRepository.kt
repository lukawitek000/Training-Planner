package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.map
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise2
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseQuery
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class DbExerciseRepository(
    private val exerciseDao: ExerciseDao,
    private val ioDispatcher: CoroutineDispatcher,
) : ExerciseRepository {
    override fun getExerciseDetailsById(id: ExerciseId): Flow<ExerciseDetails?> =
        exerciseDao.getExerciseDetailsById(id.toString()).map {
            it?.toExerciseDetails()
        }

    override fun queryExercises(query: ExerciseQuery): Flow<PagingData<Exercise2>> =
        Pager(
            config =
                PagingConfig(
                    pageSize = 10,
                    prefetchDistance = 10,
                ),
            pagingSourceFactory = { pagingSource(query) },
        ).flow.map { pagingData ->
            pagingData.map { dbExercise ->
                Timber.d("Fetched exercise: ${dbExercise.exercise.name}")
                dbExercise.toExercise()
            }
        }

    private fun pagingSource(query: ExerciseQuery): PagingSource<Int, DbExerciseWithCategories> =
        if (query.categories.isEmpty()) {
            exerciseDao.getExercisesWithCategories(query.query)
        } else {
            exerciseDao
                .getExercisesWithCategories(
                    query = query.query,
                    categoriesNames = query.categories.map { it.name },
                )
        }

    override suspend fun insert(exercise: ExerciseDetails): Boolean =
        withContext(ioDispatcher) {
            val dbExerciseDetails = exercise.toDbExerciseDetails()
            exerciseDao.insertExerciseDetails(dbExerciseDetails)
        }

    override suspend fun delete(exerciseId: ExerciseId) =
        withContext(ioDispatcher) {
            exerciseDao.deleteExerciseById(exerciseId.toString()) == ONE_ROW
        }

    override suspend fun updateExercise(exercise: ExerciseDetails): Boolean =
        withContext(ioDispatcher) {
            val dbExercise = exercise.toDbExerciseDetails()
            exerciseDao.update(dbExercise) == ONE_ROW
        }

    private companion object {
        const val ONE_ROW = 1
    }
}
