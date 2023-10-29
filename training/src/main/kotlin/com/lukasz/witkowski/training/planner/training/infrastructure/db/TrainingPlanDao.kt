package com.lukasz.witkowski.training.planner.training.infrastructure.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.lukasz.witkowski.training.planner.training.infrastructure.db.models.DbTrainingExercise
import com.lukasz.witkowski.training.planner.training.infrastructure.db.models.DbTrainingPlan
import com.lukasz.witkowski.training.planner.training.infrastructure.db.models.DbTrainingPlanWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
internal interface TrainingPlanDao {

    @Transaction
    suspend fun insertTrainingWithTrainingExercises(dbTrainingPlanWithExercises: DbTrainingPlanWithExercises) {
        insertTraining(dbTrainingPlanWithExercises.trainingPlan)
        for (dbExercise in dbTrainingPlanWithExercises.exercises) {
            insertExercise(dbExercise)
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTraining(dbTrainingPlan: DbTrainingPlan)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(dbExercise: DbTrainingExercise)

    @Query("SELECT * FROM TrainingPlan")
    fun getAll(): Flow<List<DbTrainingPlanWithExercises>>

    @Query("UPDATE TrainingPlan SET isSynchronized=1 WHERE id=:id")
    fun setTrainingPlanAsSynchronized(id: String)

    @Transaction
    suspend fun deleteTrainingPlanWithExercises(dbTrainingPlanWithExercises: DbTrainingPlanWithExercises) {
        deleteTrainingPlanById(dbTrainingPlanWithExercises.trainingPlan.id)
        for (dbExercise in dbTrainingPlanWithExercises.exercises) {
            deleteExerciseById(dbExercise.id)
        }
    }

    @Query("DELETE FROM TrainingPlan WHERE id=:id")
    suspend fun deleteTrainingPlanById(id: String)

    @Query("DELETE FROM Exercise WHERE id=:id")
    suspend fun deleteExerciseById(id: String)

    @Query("SELECT * FROM TrainingPlan WHERE id=:id")
    suspend fun getTrainingPlanById(id: String): DbTrainingPlanWithExercises
}
