package com.lukasz.witkowski.training.planner.training.infrastructure

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbExerciseCategory
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbTrainingExercise
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbTrainingOverview
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbTrainingPlan
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbTrainingPlanWithExercises
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

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
    @Query(
        """
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
        """,
    )
    fun getAllTrainingOverviews(query: String): Flow<List<DbTrainingOverview>>

    @Transaction
    @Query(
        """
        SELECT 
            p.id AS trainingPlanId, 
            p.name AS title,
            p.description,
            p.lastModified AS lastModification,
            p.lastUsed AS lastSession
        FROM TrainingPlan p
        JOIN exercise_categories c
            ON p.id = c.trainingPlanId
        WHERE c.name IN (:categoriesNames)
          AND (
              LOWER(p.name) LIKE LOWER('%' || :query || '%')
              OR LOWER(p.description) LIKE LOWER('%' || :query || '%')
          )
        GROUP BY p.id
        ORDER BY COUNT(*) DESC, p.name ASC
        """,
    )
    fun getAllTrainingOverviews(
        query: String,
        categoriesNames: List<String>,
    ): Flow<List<DbTrainingOverview>>

    @Query("DELETE FROM TrainingPlan WHERE id=:id")
    suspend fun deleteTrainingPlanById(id: String)

    @Transaction
    @Query("SELECT * FROM TrainingPlan WHERE id=:id")
    fun getFlowTrainingPlanById(id: String): Flow<DbTrainingPlanWithExercises?>

    @Transaction
    @Query("SELECT * FROM TrainingPlan WHERE id=:id")
    suspend fun getTrainingPlanById(id: String): DbTrainingPlanWithExercises?

    @Query("DELETE FROM TrainingExercise WHERE trainingId = :trainingPlanId")
    suspend fun deleteTrainingExercisesForPlan(trainingPlanId: String)

    @Upsert
    fun updateTrainingPlan(plan: DbTrainingPlan)

    @Transaction
    suspend fun updateTrainingWithTrainingExercises(dbTrainingPlanWithExercises: DbTrainingPlanWithExercises) {
        deleteTrainingExercisesForPlan(dbTrainingPlanWithExercises.trainingPlan.id)
        updateTrainingPlan(dbTrainingPlanWithExercises.trainingPlan)
        for (dbExercise in dbTrainingPlanWithExercises.exercises) {
            insertExercise(dbExercise.trainingExercise)
            for (category in dbExercise.categories) {
                insertCategory(category)
            }
        }
    }

    @Transaction
    suspend fun useTrainingPlanById(
        id: String,
        currentInstant: Instant,
    ): DbTrainingPlanWithExercises {
        val result = getTrainingPlanById(id)
        checkNotNull(result) { "Training plan was not found" }
        val updatedPlan =
            result.trainingPlan.copy(
                lastUsed = currentInstant,
            )
        updateTrainingPlan(updatedPlan)
        return result
    }

    // Testing
    @Query("SELECT COUNT(*) FROM TrainingExercise WHERE trainingId = :trainingPlanId")
    suspend fun getExercisesCountForTrainingPlan(trainingPlanId: String): Int

    // Testing
    @Query("SELECT COUNT(*) FROM exercise_categories WHERE trainingPlanId = :trainingPlanId")
    suspend fun getCategoriesCountForTrainingPlan(trainingPlanId: String): Int
}
