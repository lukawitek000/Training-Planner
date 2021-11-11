package com.lukasz.witkowski.training.wearable.currentTraining

sealed class CurrentTrainingState {
    object ExerciseState : CurrentTrainingState()
    object RestTimeState : CurrentTrainingState()
}
