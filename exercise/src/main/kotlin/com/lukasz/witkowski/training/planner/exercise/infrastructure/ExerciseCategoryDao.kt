package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.room3.Dao
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseCategoryDao {
    @Query("SELECT * FROM DbExerciseCategory")
    fun getAll(): Flow<List<DbExerciseCategory>>
}
