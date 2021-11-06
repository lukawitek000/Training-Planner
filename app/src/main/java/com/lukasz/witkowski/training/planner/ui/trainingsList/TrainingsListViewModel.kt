package com.lukasz.witkowski.training.planner.ui.trainingsList

import androidx.lifecycle.ViewModel
import com.lukasz.witkowski.shared.models.Training
import com.lukasz.witkowski.shared.models.dummyTrainingList
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TrainingsListViewModel @Inject constructor() : ViewModel(){

    fun getAllTrainings() : List<Training> {
        return dummyTrainingList
    }
}