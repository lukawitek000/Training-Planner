package com.lukasz.witkowski.training.wearable.repo

import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.training.wearable.trainingsList

class TrainingRepository {
    fun getDummyTrainings() = trainingsList

    fun fetchTrainingById(id: Long): TrainingWithExercises {
        return trainingsList.first { it.training.id == id }
    }
}
