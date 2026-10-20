package com.lukasz.witkowski.training.planner.exercise.infrastructure

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface ExerciseMutationDao {
    @Query(
        "SELECT * FROM ExerciseMutation WHERE state IN ('PENDING', 'IN_PROGRESS') ORDER BY createdAt ASC"
    )
    suspend fun getPendingMutations(): List<DbExerciseMutation>

    @Query(
        "SELECT * FROM ExerciseMutation WHERE exerciseId = :exerciseId ORDER BY createdAt DESC LIMIT 1"
    )
    fun getMutationByExerciseId(exerciseId: String): Flow<DbExerciseMutation?>

    @Query(
        "SELECT * FROM ExerciseMutation WHERE exerciseId = :exerciseId ORDER BY createdAt DESC LIMIT 1"
    )
    suspend fun getMutationByExerciseIdOnce(exerciseId: String): DbExerciseMutation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMutation(mutation: DbExerciseMutation)

    @Update
    suspend fun updateMutation(mutation: DbExerciseMutation)

    @Query("DELETE FROM ExerciseMutation WHERE id = :id")
    suspend fun deleteMutation(id: String)

    @Query("DELETE FROM ExerciseMutation WHERE exerciseId = :exerciseId")
    suspend fun deleteMutationsForExercise(exerciseId: String)

    @Query("DELETE FROM ExerciseMutation")
    suspend fun deleteAllMutations()
}
