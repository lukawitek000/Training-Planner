package com.lukasz.witkowski.training.planner.ui.createExercise

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lukasz.witkowski.shared.Exercise
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CreateExerciseViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {


    private var exercise: Exercise? = null

    private val _title = MutableLiveData("")
    val title: LiveData<String> = _title

    fun onExerciseNameChange(newName: String){
        _title.value = newName
    }

    private val _description = MutableLiveData("")
    val description: LiveData<String> = _description

    fun onExerciseDescriptionChange(newDescription: String){
        _description.value = newDescription
    }

}