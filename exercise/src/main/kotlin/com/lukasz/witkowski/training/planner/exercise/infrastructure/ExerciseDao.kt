package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface ExerciseDao {

    @Query("SELECT * FROM Exercise")
    fun getAll(): Flow<List<DbExercise>>

    @Query("SELECT * FROM Exercise WHERE :id == id")
    fun getById(id: String): Flow<DbExercise>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dbExercise: DbExercise): Long

    @Query("DELETE FROM Exercise WHERE :id == id")
    suspend fun deleteExerciseById(id: String): Int

    @Update
    suspend fun update(dbExercise: DbExercise): Int // returns number of updated rows
}
