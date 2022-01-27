package com.lukasz.witkowski.shared.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lukasz.witkowski.shared.models.Category
import com.lukasz.witkowski.shared.models.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM Exercise")
    fun getAll(): Flow<List<Exercise>>

    @Query("SELECT * FROM Exercise WHERE category IN (:categories)")
    fun getExercisesFromCategories(categories: List<Category>): Flow<List<Exercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: Exercise): Long
}