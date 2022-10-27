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
}
