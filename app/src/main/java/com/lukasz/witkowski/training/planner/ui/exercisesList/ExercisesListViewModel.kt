package com.lukasz.witkowski.training.planner.ui.exercisesList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lukasz.witkowski.shared.Exercise
import com.lukasz.witkowski.shared.dummyExerciseList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ExercisesListViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    val exercises = dummyExerciseList.toList()

}