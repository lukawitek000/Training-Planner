package com.lukasz.witkowski.training.planner.training.infrastructure

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbExerciseCategory
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbTrainingExercise
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbTrainingOverview
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbTrainingPlan
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbTrainingPlanWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
internal interface TrainingPlanDao {
    @Transaction
    suspend fun insertTrainingWithTrainingExercises(dbTrainingPlanWithExercises: DbTrainingPlanWithExercises) {
        insertTraining(dbTrainingPlanWithExercises.trainingPlan)
        for (dbExercise in dbTrainingPlanWithExercises.exercises) {
            insertExercise(dbExercise.trainingExercise)
            for (category in dbExercise.categories) {
                insertCategory(category)
            }
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTraining(dbTrainingPlan: DbTrainingPlan)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(dbExercise: DbTrainingExercise)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: DbExerciseCategory)

    @Transaction
    @Query("""
        SELECT 
         id as trainingPlanId, 
         name as title,
         description,
         lastModified as lastModification,
         lastUsed as lastSession
         FROM TrainingPlan
         WHERE LOWER(name) LIKE LOWER('%' || :query || '%')
           OR LOWER(description) LIKE LOWER('%' || :query || '%')
         ORDER BY name ASC
        """)
    fun getAllTrainingOverviews(
        query: String,
    ): Flow<List<DbTrainingOverview>>

    @Transaction
    suspend fun deleteTrainingPlanWithExercises(dbTrainingPlanWithExercises: DbTrainingPlanWithExercises) {
//        deleteTrainingPlanById(dbTrainingPlanWithExercises.trainingPlan.id)
//        for (dbExercise in dbTrainingPlanWithExercises.exercises) {
//            deleteExerciseById(dbExercise.exerciseId)
//        }
    }

    @Query("DELETE FROM TrainingPlan WHERE id=:id")
    suspend fun deleteTrainingPlanById(id: String)

    @Query("DELETE FROM TrainingExercise WHERE exerciseId=:id")
    suspend fun deleteExerciseById(id: String)

    @Transaction
    @Query("SELECT * FROM TrainingPlan WHERE id=:id")
    fun getTrainingPlanById(id: String): Flow<DbTrainingPlanWithExercises>
}
