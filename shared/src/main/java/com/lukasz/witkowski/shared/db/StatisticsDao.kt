package com.lukasz.witkowski.shared.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.lukasz.witkowski.shared.models.statistics.ExerciseStatistics
import com.lukasz.witkowski.shared.models.statistics.TrainingCompleteStatistics
import com.lukasz.witkowski.shared.models.statistics.TrainingStatistics
import kotlinx.coroutines.flow.Flow

@Dao
interface StatisticsDao {

    @Transaction
    fun insertTrainingCompleteStatistics(trainingCompleteStatistics: TrainingCompleteStatistics): Long {
        val trainingStatisticsId = insertTrainingStatistics(trainingCompleteStatistics.trainingStatistics)
        for(exerciseStatistics in trainingCompleteStatistics.exercisesStatistics) {
            exerciseStatistics.trainingStatisticsId = trainingStatisticsId
            insertExerciseStatistics(exerciseStatistics)
        }
        return trainingStatisticsId
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExerciseStatistics(exerciseStatistics: ExerciseStatistics)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTrainingStatistics(trainingStatistics: TrainingStatistics): Long

    @Query("SELECT * FROM TrainingStatistics WHERE isSynchronized=0")
    fun getNotSynchronizedStatistics(): Flow<List<TrainingCompleteStatistics>>

    @Query("SELECT * FROM TrainingStatistics WHERE trainingId=:trainingId")
    fun getTrainingCompleteStatisticsByTrainingId(trainingId: Long) : List<TrainingCompleteStatistics>

    @Query("UPDATE TrainingStatistics SET isSynchronized=1 WHERE TrainingIdStatistics=:id")
    fun updateSynchronizedStatisticsById(id: Long)
}