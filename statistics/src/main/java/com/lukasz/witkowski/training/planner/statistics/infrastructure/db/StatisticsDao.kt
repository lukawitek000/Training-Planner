package com.lukasz.witkowski.training.planner.statistics.infrastructure.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.models.DbExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.models.DbExerciseStatistics
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.models.DbTrainingStatistics
import com.lukasz.witkowski.training.planner.statistics.infrastructure.db.models.DbTrainingWithExercisesStatistics
import kotlinx.coroutines.flow.Flow

@Dao
interface StatisticsDao {

    @Transaction
    suspend fun insertAllStatistics(dbTrainingWithExercisesStatistics: DbTrainingWithExercisesStatistics) {
        val dbTrainingStatistics = dbTrainingWithExercisesStatistics.trainingStatistics
        insert(dbTrainingStatistics)
        for (exercisesStatistics in dbTrainingWithExercisesStatistics.exercisesStatistics) {
            insert(exercisesStatistics.exerciseStatistics)
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

    @Transaction
    suspend fun deleteTrainingPlanStatistics(trainingPlanId: String) {
        val statisticsIds = getStatisticsIdsFromTrainingPlan(trainingPlanId)
        val exerciseStatisticsIds = getExerciseStatisticsIdsFromStatistics(statisticsIds)
        deleteExerciseAttemptStatistics(exerciseStatisticsIds)
        deleteExerciseStatistics(exerciseStatisticsIds)
        deleteTrainingStatistics(statisticsIds)
    }

    @Query("SELECT id FROM TrainingStatistics WHERE trainingPlanId = :trainingPlanId")
    suspend fun getStatisticsIdsFromTrainingPlan(trainingPlanId: String): List<String>

    @Query("SELECT id FROM ExerciseStatistics WHERE trainingStatisticsId IN (:statisticsIds)")
    suspend fun getExerciseStatisticsIdsFromStatistics(statisticsIds: List<String>): List<String>

    @Query("DELETE FROM ExerciseAttemptStatistics WHERE exerciseStatisticsId IN (:exerciseStatisticsIds)")
    suspend fun deleteExerciseAttemptStatistics(exerciseStatisticsIds: List<String>)

    @Query("DELETE FROM ExerciseStatistics WHERE id IN (:exerciseStatisticsIds)")
    suspend fun deleteExerciseStatistics(exerciseStatisticsIds: List<String>)

    @Query("DELETE FROM TrainingStatistics WHERE id IN (:statisticsIds)")
    suspend fun deleteTrainingStatistics(statisticsIds: List<String>)
}
