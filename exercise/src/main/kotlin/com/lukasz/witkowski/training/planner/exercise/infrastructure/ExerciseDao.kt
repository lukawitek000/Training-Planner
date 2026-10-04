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
        WHERE LOWER(name) LIKE LOWER('%' || :query || '%')
           OR LOWER(description) LIKE LOWER('%' || :query || '%')
        ORDER BY name ASC
    """,
    )
    fun getExercisesWithCategories(query: String): PagingSource<Int, DbExerciseWithCategories>

    @Transaction
    @Query(
        """
            SELECT e.*
            FROM Exercise e
            JOIN ExerciseCategoryCrossRef r
                ON e.exerciseId = r.exerciseId
            WHERE r.categoryName IN (:categoriesNames)
              AND (
                  LOWER(e.name) LIKE LOWER('%' || :query || '%')
                  OR LOWER(e.description) LIKE LOWER('%' || :query || '%')
              )
            GROUP BY e.exerciseId
            ORDER BY COUNT(*) DESC, e.name ASC
        """,
    )
    fun getExercisesWithCategories(
        query: String,
        categoriesNames: List<String>,
    ): PagingSource<Int, DbExerciseWithCategories>

    @Transaction
    @Query("SELECT * FROM Exercise WHERE :id == exerciseId")
    fun getExerciseDetailsById(id: String): Flow<DbExerciseDetails?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dbExercise: DbExercise): Long

    @Transaction
    suspend fun insertExerciseDetails(details: DbExerciseDetails): Boolean {
        val exRow = insert(details.exercise)
        if (exRow == -1L) return false
        details.categories.forEach {
            insert(it)
        }
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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(recommendations: List<DbExerciseRecommendation>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: DbExerciseCategory)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExerciseCategoryCrossRefs(crossRefs: List<ExerciseCategoryCrossRef>): List<Long>

    @Query("DELETE FROM Exercise WHERE :id == exerciseId")
    suspend fun deleteExerciseById(id: String): Int

    @Query(
        """
            DELETE FROM ExerciseCategoryCrossRef
            WHERE exerciseId = :exerciseId
        """,
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
}
