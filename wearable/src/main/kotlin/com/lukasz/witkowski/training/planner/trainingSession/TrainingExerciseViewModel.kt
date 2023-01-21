package com.lukasz.witkowski.training.planner.trainingSession

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingExercise
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TrainingExerciseViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle): ViewModel() {

    private var _currentExercise: TrainingExercise? = null
    val currentExercise: TrainingExercise
        get() = _currentExercise ?: throw IllegalStateException("Unknown current exercise")

    fun setCurrentExercise(exercise: TrainingExercise) {
        _currentExercise = exercise
    }

}
