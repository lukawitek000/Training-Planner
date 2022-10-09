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

    @Transaction
    @Query("SELECT * FROM Exercise")
    fun getAll(): Flow<List<ExerciseWithImage>>

    @Transaction
    @Query("SELECT * FROM Exercise WHERE :id == id")
    fun getById(id: String): Flow<ExerciseWithImage>


    @Transaction
    suspend fun insert(exerciseWithImage: ExerciseWithImage): Long {
        exerciseWithImage.imageReference?.let { insert(it) }
        return insert(exerciseWithImage.exercise)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dbExercise: DbExercise): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dbImageReference: DbImageReference): Long

    @Transaction
    suspend fun deleteExerciseWithImage(exerciseId: String) {
        deleteImageReferenceByExerciseId(exerciseId)
        deleteExerciseById(exerciseId)
    }

    @Query("DELETE FROM Exercise WHERE :id == id")
    suspend fun deleteExerciseById(id: String)

    @Transaction
    suspend fun update(exerciseWithImage: ExerciseWithImage): Boolean {
        val imgReference = exerciseWithImage.imageReference
        deleteImageReferenceByExerciseId(exerciseWithImage.exercise.id)
        imgReference?.let { insert(it) }
        update(exerciseWithImage.exercise)
        return true
    }

    @Update
    suspend fun update(dbExercise: DbExercise): Int // returns number of updated rows

    @Query("DELETE FROM DBIMAGEREFERENCE WHERE exerciseId = :exerciseId")
    suspend fun deleteImageReferenceByExerciseId(exerciseId: String)
}
