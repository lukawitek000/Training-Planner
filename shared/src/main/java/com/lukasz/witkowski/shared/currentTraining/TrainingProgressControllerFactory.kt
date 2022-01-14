package com.lukasz.witkowski.shared.currentTraining

import com.lukasz.witkowski.shared.models.TrainingWithExercises
import dagger.assisted.AssistedFactory

@AssistedFactory
interface TrainingProgressControllerFactory {
    fun create(trainingWithExercises: TrainingWithExercises): TrainingProgressController
}