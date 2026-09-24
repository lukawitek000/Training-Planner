package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
internal interface ExerciseDao {
    @Query("SELECT * FROM Exercise")
    fun getAll(): Flow<List<DbExercise>>

    @Transaction
    @Query("""
        SELECT 
            e.*,
            COUNT(*) AS matched_cat
        FROM Exercise e
        JOIN ExerciseCategoryCrossRef r
            ON e.exerciseId = r.exerciseId
        WHERE (:filterByCategories = 0 OR r.categoryName IN (:categoriesNames))
            AND (
              LOWER(e.name) LIKE LOWER('%' || :query || '%')
              OR LOWER(e.description) LIKE LOWER('%' || :query || '%')
            )
        GROUP BY e.exerciseId
        ORDER BY matched_cat DESC
    """)
    fun getExercisesWithCategories(
        query: String,
        categoriesNames: List<String>,
        filterByCategories: Boolean,
    ): Flow<List<DbExerciseWithCategories>>

    @Query("SELECT * FROM Exercise WHERE :id == exerciseId")
    suspend fun getById(id: String): DbExerciseWithCategories

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dbExercise: DbExercise): Long

    @Transaction
    suspend fun insertExerciseWithCategories(exerciseWithCategories: DbExerciseWithCategories): Boolean {
        val exRow = insert(exerciseWithCategories.exercise)
        if (exRow == -1L) return false
        exerciseWithCategories.categories.forEach {
            insert(it)
        }
        val crossRefs = exerciseWithCategories.categories.map {
            ExerciseCategoryCrossRef(exerciseWithCategories.exercise.exerciseId, it.categoryName)
        }
        val insertedRowIds = insertExerciseCategoryCrossRefs(crossRefs)
        return insertedRowIds.all {
            it != -1L
        }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: DbExerciseCategory)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExerciseCategoryCrossRefs(crossRefs: List<ExerciseCategoryCrossRef>): List<Long>

    @Query("DELETE FROM Exercise WHERE :id == exerciseId")
    suspend fun deleteExerciseById(id: String): Int

    @Update
    suspend fun update(dbExercise: DbExercise): Int // returns number of updated rows
}
