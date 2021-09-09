package com.lukasz.witkowski.training.planner.ui.TrainingsList

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lukasz.witkowski.shared.Training
import com.lukasz.witkowski.shared.dummyTrainingList
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TrainingsListViewModel @Inject constructor() : ViewModel(){

    fun getAllTrainings() : List<Training> {
        return dummyTrainingList
    }
}