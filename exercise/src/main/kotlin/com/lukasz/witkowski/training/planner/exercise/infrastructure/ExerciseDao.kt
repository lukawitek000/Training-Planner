package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface ExerciseDao {

    @Query("SELECT * FROM Exercise")
    fun getAll(): Flow<List<DbExercise>>

    @Query("SELECT * FROM Exercise WHERE :id == id")
    suspend fun getById(id: String): DbExercise

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dbExercise: DbExercise): Long

    @Query("DELETE FROM Exercise WHERE :id == id")
    suspend fun deleteExerciseById(id: String): Int

    @Update
    suspend fun update(dbExercise: DbExercise): Int // returns number of updated rows
}
