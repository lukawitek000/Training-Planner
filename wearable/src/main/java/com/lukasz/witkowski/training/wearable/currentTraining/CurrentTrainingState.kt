package com.lukasz.witkowski.training.wearable.currentTraining

import com.lukasz.witkowski.shared.models.TrainingExercise

sealed class CurrentTrainingState {
    data class ExerciseState(val exercise: TrainingExercise) : CurrentTrainingState()
    data class RestTimeState(val restTime: Long) : CurrentTrainingState()
    object SummaryState : CurrentTrainingState()
}
