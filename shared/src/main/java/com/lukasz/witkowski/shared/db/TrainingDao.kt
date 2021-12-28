package com.lukasz.witkowski.shared.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import com.lukasz.witkowski.shared.models.Training
import com.lukasz.witkowski.shared.models.TrainingExercise
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingDao {

    @Query("SELECT * FROM Training")
    fun getAllTrainings(): Flow<List<Training>>

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
}