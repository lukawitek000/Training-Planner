package com.lukasz.witkowski.training.planner.exercise.infrastructure.local

import androidx.paging.PagingSource
import com.lukasz.witkowski.training.planner.exercise.infrastructure.DbExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.infrastructure.DbExerciseMutation
import com.lukasz.witkowski.training.planner.exercise.infrastructure.DbExerciseWithCategories
import com.lukasz.witkowski.training.planner.exercise.infrastructure.ExerciseDao
import com.lukasz.witkowski.training.planner.exercise.infrastructure.ExerciseMutationDao
import kotlinx.coroutines.flow.Flow

internal class RoomExerciseLocalDataSource(
    private val exerciseDao: ExerciseDao,
    private val mutationDao: ExerciseMutationDao
) : ExerciseLocalDataSource {

    override fun getExerciseDetailsById(id: String): Flow<DbExerciseDetails?> =
        exerciseDao.getExerciseDetailsById(id)

    override fun getExercisesWithCategories(
        query: String,
        categoriesNames: List<String>
    ): PagingSource<Int, DbExerciseWithCategories> = if (categoriesNames.isEmpty()) {
        exerciseDao.getExercisesWithCategories(query)
    } else {
        exerciseDao.getExercisesWithCategories(query, categoriesNames)
    }

    override suspend fun insertExerciseDetails(details: DbExerciseDetails): Boolean =
        exerciseDao.insertExerciseDetails(details)

    override suspend fun updateExerciseDetails(details: DbExerciseDetails): Boolean =
        exerciseDao.update(details) == ONE_ROW

    override suspend fun deleteExerciseById(id: String): Boolean =
        exerciseDao.deleteExerciseAndCascade(id) > 0

    override suspend fun getMutationByExerciseIdOnce(exerciseId: String): DbExerciseMutation? =
        mutationDao.getMutationByExerciseIdOnce(exerciseId)

    override suspend fun insertMutation(mutation: DbExerciseMutation) {
        mutationDao.deleteMutationsForExercise(mutation.exerciseId)
        mutationDao.insertMutation(mutation)
    }

    override suspend fun updateMutation(mutation: DbExerciseMutation) =
        mutationDao.updateMutation(mutation)

    override suspend fun deleteMutationsForExercise(exerciseId: String) =
        mutationDao.deleteMutationsForExercise(exerciseId)

    override suspend fun updateSyncState(exerciseId: String, syncState: String) =
        exerciseDao.updateSyncState(exerciseId, syncState)

    private companion object {
        const val ONE_ROW = 1
    }
}
