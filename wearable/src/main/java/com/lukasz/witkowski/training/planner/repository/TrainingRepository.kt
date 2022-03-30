package com.lukasz.witkowski.training.planner.repository

import com.lukasz.witkowski.shared.db.StatisticsDao
import com.lukasz.witkowski.shared.models.statistics.TrainingCompleteStatistics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

//class TrainingRepository(
//    private val trainingDao: TrainingDao,
//    private val statisticsDao: StatisticsDao
//) {
//
//    suspend fun fetchTrainingById(id: Long): TrainingWithExercises = withContext(Dispatchers.IO) {
//        trainingDao.getTrainingWithExercisesByIdAsync(id)
//    }
//
//    suspend fun insertTrainingCompleteStatistics(trainingCompleteStatistics: TrainingCompleteStatistics): Long =
//        withContext(Dispatchers.IO) {
//            statisticsDao.insertTrainingCompleteStatistics(trainingCompleteStatistics)
//        }
//
//
//    fun getAllTrainingsWithExercises(): Flow<List<TrainingWithExercises>> {
//        return trainingDao.getAllTrainingsWithExercises()
//    }
//}
