package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lukasz.witkowski.shared.models.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM Exercise")
    fun getAll(): Flow<List<DbExercise>>

    @Query("SELECT * FROM Exercise WHERE :id == id")
    fun getById(id: String): Flow<DbExercise>

    @Query("SELECT * FROM Exercise WHERE category IN (:categories)")
    fun getExercisesFromCategories(categories: List<Category>): Flow<List<DbExercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: DbExercise): Long

    @Delete
    suspend fun delete(exercise: DbExercise)
}