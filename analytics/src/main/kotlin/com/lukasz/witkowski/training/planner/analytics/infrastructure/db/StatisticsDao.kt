package com.lukasz.witkowski.training.planner.analytics.infrastructure.db

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import com.lukasz.witkowski.training.planner.analytics.infrastructure.db.models.DbExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.analytics.infrastructure.db.models.DbExerciseStatistics
import com.lukasz.witkowski.training.planner.analytics.infrastructure.db.models.DbTrainingStatistics
import com.lukasz.witkowski.training.planner.analytics.infrastructure.db.models.DbTrainingWithExercisesStatistics
import kotlinx.coroutines.flow.Flow

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
interface StatisticsDao {
    @Transaction
    suspend fun insertAllStatistics(
        dbTrainingWithExercisesStatistics: DbTrainingWithExercisesStatistics
    ) {
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

    @Query("UPDATE TrainingStatistics SET rpe = :rpe, note = :note WHERE id = :id")
    suspend fun updateMetadata(id: String, rpe: Int?, note: String?)

    @Transaction
    @Query(
        """
        SELECT * FROM TrainingStatistics
        WHERE trainingPlanId = :trainingPlanId
        ORDER BY date DESC
        """
    )
    fun getTrainingStatisticsByTrainingPlanId(
        trainingPlanId: String
    ): Flow<List<DbTrainingWithExercisesStatistics>>

    @Transaction
    @Query(
        """
        SELECT * FROM TrainingStatistics
        WHERE trainingPlanId = :trainingPlanId
        ORDER BY date DESC
        """
    )
    fun getTrainingStatisticsOverviewsByTrainingPlanId(
        trainingPlanId: String
    ): PagingSource<Int, DbTrainingWithExercisesStatistics>

    @Transaction
    @Query("SELECT * FROM TrainingStatistics WHERE id=:id")
    fun getTrainingStatisticsById(id: String): Flow<DbTrainingWithExercisesStatistics?>
}
