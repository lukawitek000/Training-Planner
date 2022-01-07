package com.lukasz.witkowski.training.planner.repo

import com.lukasz.witkowski.shared.db.TrainingDao
import com.lukasz.witkowski.shared.models.TrainingStatistics
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.training.planner.trainingsList

class TrainingRepository(
    private val trainingDao: TrainingDao
) {
    fun getDummyTrainings() = trainingsList

    fun fetchTrainingById(id: Long): TrainingWithExercises {
        return trainingsList.first { it.training.id == id }
    }

    fun insertTrainingStatistics(trainingStatistics: TrainingStatistics): Long {
        // TODO insert statistics and return its id
        return 0L
    }

}
