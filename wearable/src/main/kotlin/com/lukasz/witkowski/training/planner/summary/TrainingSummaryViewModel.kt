package com.lukasz.witkowski.training.planner.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

class TrainingSummaryViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel()

