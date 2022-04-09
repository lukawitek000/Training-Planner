package com.lukasz.witkowski.training.planner.statistics.infrastructure.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface StatisticsDao {

    @Transaction
    suspend fun insertAllStatistics(dbTrainingStatistics: DbTrainingStatistics) {
        insert(dbTrainingStatistics)
        for (exercisesStatistics in dbTrainingStatistics.exercisesStatistics) {
            insert(exercisesStatistics)
            for (attemptStatistics in exercisesStatistics.exerciseAttemptsStatistics) {
                insert(attemptStatistics)
            }
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dbTrainingStatistics: DbTrainingStatistics)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dbExerciseAttemptStatistics: DbExerciseAttemptStatistics)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dbExerciseStatistics: DbExerciseStatistics)

    @Query("SELECT * FROM TrainingStatistics WHERE trainingPlanId=:trainingPlanId")
    fun getTrainingStatistics(trainingPlanId: String): Flow<List<DbTrainingWithExercisesStatistics>>
}
