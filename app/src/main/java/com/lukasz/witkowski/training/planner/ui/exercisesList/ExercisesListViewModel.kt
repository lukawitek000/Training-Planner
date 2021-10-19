package com.lukasz.witkowski.training.planner.ui.exercisesList

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lukasz.witkowski.shared.models.dummyExerciseList
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExercisesListViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    val exercises = dummyExerciseList.toList()

}