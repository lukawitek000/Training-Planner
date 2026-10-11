package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.lukasz.witkowski.training.planner.exercise.domain.Exercise2
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseQuery
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepository
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseRepositoryConfig
import com.lukasz.witkowski.training.planner.exercise.infrastructure.local.ExerciseLocalDataSource
import com.lukasz.witkowski.training.planner.exercise.infrastructure.remote.ExerciseRemoteDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class DefaultExerciseRepository(
    private val localDataSource: ExerciseLocalDataSource,
    private val remoteDataSource: ExerciseRemoteDataSource,
    private val config: ExerciseRepositoryConfig = ExerciseRepositoryConfig(),
    private val ioDispatcher: CoroutineDispatcher
) : ExerciseRepository {

    override fun getExerciseDetailsById(id: ExerciseId): Flow<ExerciseDetails?> =
        localDataSource.getExerciseDetailsById(id.toString()).map {
            it?.toExerciseDetails()
        }

    override fun queryExercises(query: ExerciseQuery): Flow<PagingData<Exercise2>> = Pager(
        config = PagingConfig(
            pageSize = config.pagingConfig.pageSize,
            prefetchDistance = config.pagingConfig.prefetchDistance
        ),
        pagingSourceFactory = {
            localDataSource.getExercisesWithCategories(
                query = query.query,
                categoriesNames = query.categories.map { it.name }
            )
        }
    ).flow.map { pagingData ->
        pagingData.map { dbExercise ->
            Timber.d("Fetched exercise: ${dbExercise.exercise.name}")
            dbExercise.toExercise()
        }
    }

    override suspend fun insert(exercise: ExerciseDetails): Boolean = withContext(ioDispatcher) {
        val dbExerciseDetails = exercise.toDbExerciseDetails()
        localDataSource.insertExerciseDetails(dbExerciseDetails)
    }

    override suspend fun delete(exerciseId: ExerciseId): Boolean = withContext(ioDispatcher) {
        localDataSource.deleteExerciseById(exerciseId.toString())
    }

    override suspend fun updateExercise(exercise: ExerciseDetails): Boolean =
        withContext(ioDispatcher) {
            val dbExerciseDetails = exercise.toDbExerciseDetails()
            localDataSource.updateExerciseDetails(dbExerciseDetails)
        }

    override suspend fun saveExerciseLocally(exerciseDetails: ExerciseDetails): Boolean =
        withContext(ioDispatcher) {
            val dbExerciseDetails = exerciseDetails.toDbExerciseDetails()
            localDataSource.insertExerciseDetails(dbExerciseDetails)
        }
}
