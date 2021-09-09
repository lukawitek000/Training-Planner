package com.lukasz.witkowski.training.planner.ui.CreateExercise

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lukasz.witkowski.shared.Exercise
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CreateExerciseViewModel @Inject constructor(): ViewModel() {


    private var exercise: Exercise? = null

    var exerciseName = ""



}