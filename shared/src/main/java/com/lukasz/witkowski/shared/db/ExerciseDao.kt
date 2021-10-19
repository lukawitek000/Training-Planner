package com.lukasz.witkowski.shared.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lukasz.witkowski.shared.models.Exercise

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM Exercise")
    fun getAll(): List<Exercise>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: Exercise): Long

}