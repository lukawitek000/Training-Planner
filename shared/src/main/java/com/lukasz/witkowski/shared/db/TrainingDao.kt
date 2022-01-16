package com.lukasz.witkowski.shared.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import com.lukasz.witkowski.shared.models.Category
import com.lukasz.witkowski.shared.models.Training
import com.lukasz.witkowski.shared.models.TrainingExercise
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingDao {

    @Query("SELECT * FROM Training")
    fun getAllTrainings(): Flow<List<Training>>

    @Query("SELECT * FROM Training")
    fun getAllTrainingsWithExercises(): Flow<List<TrainingWithExercises>>

    @Transaction
    fun insertTrainingWithTrainingExercises(trainingWithExercises: TrainingWithExercises) {
        val trainingId = insertTraining(trainingWithExercises.training)
        for (exercise in trainingWithExercises.exercises) {
            exercise.trainingId = trainingId
            insertTrainingExercise(exercise)
        }
    }

    @Insert(onConflict = REPLACE)
    fun insertTraining(training: Training): Long

    @Insert(onConflict = REPLACE)
    fun insertTrainingExercise(trainingExercise: TrainingExercise): Long

    @Query("SELECT * FROM TRAINING WHERE isSynchronized=0")
    fun getNotSynchronizedTrainingsWithExercises(): Flow<List<TrainingWithExercises>>

    @Query("SELECT * FROM TRAINING WHERE id=:id")
    fun getTrainingWithExercisesById(id: Long): Flow<TrainingWithExercises>

    @Query("SELECT * FROM TRAINING WHERE id=:id")
    suspend fun getTrainingWithExercisesByIdAsync(id: Long): TrainingWithExercises

    @Query("SELECT * FROM TRAINING WHERE id in (SELECT trainingId FROM TrainingExercise WHERE category IN (:filterCategories))")
    fun getTrainingsWithExercisesFromCategories(filterCategories: List<Category>): Flow<List<TrainingWithExercises>>

    @Query("UPDATE training SET isSynchronized=1 WHERE id=:id")
    fun updateSynchronizedTrainingById(id: Long)

}