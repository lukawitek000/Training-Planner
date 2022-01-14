package com.lukasz.witkowski.shared.currentTraining

import com.lukasz.witkowski.shared.models.TrainingExercise

sealed class CurrentTrainingState {
    data class ExerciseState(val exercise: TrainingExercise) : CurrentTrainingState()
    data class RestTimeState(val restTime: Long, val trainingId: Long) : CurrentTrainingState()
    object SummaryState : CurrentTrainingState()
}
