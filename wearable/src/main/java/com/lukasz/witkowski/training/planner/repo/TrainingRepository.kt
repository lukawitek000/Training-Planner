package com.lukasz.witkowski.training.planner.repo

import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.training.planner.trainingsList

class TrainingRepository {
    fun getDummyTrainings() = trainingsList

    fun fetchTrainingById(id: Long): TrainingWithExercises {
        return trainingsList.first { it.training.id == id }
    }
}
