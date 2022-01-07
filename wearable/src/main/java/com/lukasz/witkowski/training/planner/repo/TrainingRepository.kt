package com.lukasz.witkowski.training.planner.repo

import com.lukasz.witkowski.shared.db.TrainingDao
import com.lukasz.witkowski.shared.models.Training
import com.lukasz.witkowski.shared.models.TrainingStatistics
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.training.planner.trainingsList
import com.lukasz.witkowski.training.planner.trainingsList.TrainingsListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TrainingRepository(
    private val trainingDao: TrainingDao
) {
    fun getDummyTrainings() = trainingsList

    fun getAllTrainings(): Flow<List<Training>> {
        return trainingDao.getAllTrainings()
    }

    fun fetchDummyTrainingById(trainingId: Long): TrainingWithExercises {
        return trainingsList.first { it.training.id == trainingId}
    }

    suspend fun fetchTrainingById(id: Long): TrainingWithExercises = withContext(Dispatchers.IO) {
        trainingDao.getTrainingWithExercisesById(id)
    }

    fun insertTrainingStatistics(trainingStatistics: TrainingStatistics): Long {
        // TODO insert statistics and return its id
        return 0L
    }

    suspend fun insertTrainingWithExercises(trainingWithExercises: TrainingWithExercises) {
        withContext(Dispatchers.IO) {
            trainingDao.insertTrainingWithTrainingExercises(trainingWithExercises)
        }
    }

    fun getAllTrainingsWithExercises(): Flow<List<TrainingWithExercises>> {
        return trainingDao.getAllTrainingsWithExercises()
    }



}
