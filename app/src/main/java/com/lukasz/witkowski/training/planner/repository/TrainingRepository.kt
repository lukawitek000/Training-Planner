package com.lukasz.witkowski.training.planner.repository

import com.lukasz.witkowski.shared.db.TrainingDao
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.training.planner.exercise.domain.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TrainingRepository constructor(
    private val trainingDao: TrainingDao
) {

    fun getAllTrainingsWithExercises() = trainingDao.getAllTrainingsWithExercises()

    suspend fun insertTrainingWithExercises(trainingWithExercises: TrainingWithExercises) {
        withContext(Dispatchers.IO) {
            trainingDao.insertTrainingWithTrainingExercises(trainingWithExercises = trainingWithExercises)
        }
    }

    fun getTrainingById(trainingId: Long): Flow<TrainingWithExercises> {
        return trainingDao.getTrainingWithExercisesById(trainingId)
    }

    fun loadTrainingsWithExercises(filterCategories: List<Category>) : Flow<List<TrainingWithExercises>>{
        return if (filterCategories.isEmpty()) {
            trainingDao.getAllTrainingsWithExercises()
        } else {
            trainingDao.getTrainingsWithExercisesFromCategories(filterCategories.map { it.name })
        }
    }
}