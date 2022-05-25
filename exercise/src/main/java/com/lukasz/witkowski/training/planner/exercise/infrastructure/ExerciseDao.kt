package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface ExerciseDao {

    @Query("SELECT * FROM Exercise")
    fun getAll(): Flow<List<DbExercise>>

    @Query("SELECT * FROM Exercise WHERE :id == id")
    fun getById(id: String): Flow<DbExercise>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: DbExercise): Long

    @Query("DELETE FROM Exercise WHERE :id == id")
    suspend fun delete(id: String)

    @Update
    suspend fun update(dbExercise: DbExercise): Int // returns number of updated rows

    @Query("SELECT id FROM Exercise WHERE (name = :name AND description = :description AND categoryId = :category)")
    suspend fun getExerciseId(name: String, description: String, category: Int): String?
}
