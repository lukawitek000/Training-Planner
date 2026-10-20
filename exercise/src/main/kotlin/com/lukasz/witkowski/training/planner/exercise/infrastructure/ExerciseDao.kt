package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import kotlinx.coroutines.flow.Flow

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
internal interface ExerciseDao {
    @Query("SELECT * FROM Exercise")
    fun getAll(): Flow<List<DbExercise>>

    @Transaction
    @Query(
        """
        SELECT *
        FROM Exercise
        WHERE syncState != 'PENDING_DELETE'
          AND (
              LOWER(name) LIKE LOWER('%' || :query || '%')
              OR LOWER(description) LIKE LOWER('%' || :query || '%')
          )
        ORDER BY name ASC
    """
    )
    fun getExercisesWithCategories(query: String): PagingSource<Int, DbExerciseWithCategories>

    @Transaction
    @Query(
        """
            SELECT e.*
            FROM Exercise e
            JOIN ExerciseCategoryCrossRef r
                ON e.exerciseId = r.exerciseId
            WHERE e.syncState != 'PENDING_DELETE'
              AND r.categoryName IN (:categoriesNames)
              AND (
                  LOWER(e.name) LIKE LOWER('%' || :query || '%')
                  OR LOWER(e.description) LIKE LOWER('%' || :query || '%')
              )
            GROUP BY e.exerciseId
            ORDER BY COUNT(*) DESC, e.name ASC
        """
    )
    fun getExercisesWithCategories(
        query: String,
        categoriesNames: List<String>
    ): PagingSource<Int, DbExerciseWithCategories>

    @Transaction
    @Query("SELECT * FROM Exercise WHERE :id == exerciseId")
    fun getExerciseDetailsById(id: String): Flow<DbExerciseDetails?>

    @Query("SELECT * FROM Exercise WHERE exerciseId = :id")
    suspend fun getExerciseByIdOnce(id: String): DbExercise?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dbExercise: DbExercise): Long

    @Transaction
    suspend fun insertExerciseDetails(details: DbExerciseDetails): Boolean {
        val exRow = insert(details.exercise)
        if (exRow == -1L) return false
        details.categories.forEach {
            insert(it)
        }
        deleteCategoryReferences(details.exercise.exerciseId)
        val crossRefs =
            details.categories.map {
                ExerciseCategoryCrossRef(details.exercise.exerciseId, it.categoryName)
            }
        val insertedRowIds = insertExerciseCategoryCrossRefs(crossRefs)
        if (insertedRowIds.any { it == -1L }) return false
        val insertedRecRowIds = insert(details.recommendations)
        return insertedRecRowIds.all {
            it != -1L
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recommendations: List<DbExerciseRecommendation>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: DbExerciseCategory)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExerciseCategoryCrossRefs(
        crossRefs: List<ExerciseCategoryCrossRef>
    ): List<Long>

    @Query("DELETE FROM Exercise WHERE :id == exerciseId")
    suspend fun deleteExerciseById(id: String): Int

    @Query("DELETE FROM ExerciseMutation WHERE exerciseId = :exerciseId")
    suspend fun deleteExerciseMutations(exerciseId: String)

    @Transaction
    suspend fun deleteExerciseAndCascade(id: String): Int {
        deleteCategoryReferences(id)
        deleteExerciseMutations(id)
        return deleteExerciseById(id)
    }

    @Query(
        """
            DELETE FROM ExerciseCategoryCrossRef
            WHERE exerciseId = :exerciseId
        """
    )
    suspend fun deleteCategoryReferences(exerciseId: String)

    @Update
    suspend fun update(exercise: DbExercise): Int

    @Update
    suspend fun updateRecommendations(recommendations: List<DbExerciseRecommendation>): Int

    @Transaction
    suspend fun update(exerciseDetails: DbExerciseDetails): Int {
        val updatedRows = update(exerciseDetails.exercise)
        if (updatedRows == 0) return 0
        updateRecommendations(exerciseDetails.recommendations)
        deleteCategoryReferences(exerciseDetails.exercise.exerciseId)
        val crossRefs =
            exerciseDetails.categories.map {
                ExerciseCategoryCrossRef(exerciseDetails.exercise.exerciseId, it.categoryName)
            }
        insertExerciseCategoryCrossRefs(crossRefs)
        return updatedRows
    }

    @Query("UPDATE Exercise SET syncState = :syncState WHERE exerciseId = :exerciseId")
    suspend fun updateSyncState(exerciseId: String, syncState: String)

    @Query("SELECT value FROM SyncMetadata WHERE key = 'exercise_sync_version'")
    suspend fun getSyncVersion(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncMetadata(metadata: DbSyncMetadata)

    @Query("DELETE FROM SyncMetadata WHERE key = 'exercise_sync_version'")
    suspend fun deleteSyncVersion()

    @Query("DELETE FROM Exercise WHERE sourceType != 'BUNDLED'")
    suspend fun deleteAllNonBundledExercises()

    @Query("DELETE FROM Exercise WHERE syncState = 'PENDING_DELETE'")
    suspend fun deletePendingDeleteExercises()

    @Query(
        "UPDATE Exercise SET syncState = 'LOCAL_ONLY', ownerId = 'local_user', ownerName = 'Local', revision = 1 WHERE sourceType != 'BUNDLED'"
    )
    suspend fun convertNonBundledExercisesToLocalOnly()

    @Transaction
    suspend fun applySyncChanges(
        upserts: List<DbExerciseDetails>,
        deletedIds: List<String>,
        newVersion: Long
    ) {
        val protectedStates = setOf(
            DbExercise.SYNC_STATE_PENDING_UPLOAD,
            DbExercise.SYNC_STATE_PENDING_DELETE,
            DbExercise.SYNC_STATE_CONFLICT
        )
        upserts.forEach { item ->
            val existing = getExerciseByIdOnce(item.exercise.exerciseId)
            if (existing != null && existing.syncState in protectedStates) {
                return@forEach
            }
            val mergedExercise = if (item.exercise.imageId == null && existing?.imageId != null) {
                item.exercise.copy(imageId = existing.imageId)
            } else {
                item.exercise
            }
            insertExerciseDetails(item.copy(exercise = mergedExercise))
        }
        deletedIds.forEach { id ->
            val existing = getExerciseByIdOnce(id)
            if (existing == null || existing.syncState !in protectedStates) {
                deleteExerciseAndCascade(id)
            }
        }
        insertSyncMetadata(DbSyncMetadata(key = "exercise_sync_version", value = newVersion))
    }
}
