package com.lukasz.witkowski.training.planner.training.infrastructure.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import com.lukasz.witkowski.training.planner.training.infrastructure.db.models.DbExercise
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

    @Insert(onConflict = REPLACE)
    suspend fun insertTraining(dbTrainingPlan: DbTrainingPlan)

    @Insert(onConflict = REPLACE)
    suspend fun insertExercise(dbExercise: DbExercise)

    @Query("SELECT * FROM TrainingPlan")
    fun getAll(): Flow<List<DbTrainingPlanWithExercises>>


    @Transaction
    suspend fun deleteTrainingPlanWithExercises(dbTrainingPlanWithExercises: DbTrainingPlanWithExercises) {
        deleteTrainingPlanById(dbTrainingPlanWithExercises.trainingPlan.id)
        for(dbExercise in dbTrainingPlanWithExercises.exercises) {
            deleteExerciseById(dbExercise.id)
        }
    }

    @Query("DELETE FROM TrainingPlan WHERE id=:id")
    suspend fun deleteTrainingPlanById(id: String)

    @Query("DELETE FROM Exercise WHERE id=:id")
    suspend fun deleteExerciseById(id: String)
}