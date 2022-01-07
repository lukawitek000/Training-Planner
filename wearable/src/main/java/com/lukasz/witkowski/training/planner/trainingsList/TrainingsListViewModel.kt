package com.lukasz.witkowski.training.planner.trainingsList

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lukasz.witkowski.training.planner.repo.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TrainingsListViewModel
    @Inject constructor(
        private val savedStateHandle: SavedStateHandle,
        private val trainingRepository: TrainingRepository
    ): ViewModel() {

        val trainings = trainingRepository.getAllTrainingsWithExercises()

}