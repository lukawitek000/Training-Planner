package com.lukasz.witkowski.training.planner.ui.trainingOverview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lukasz.witkowski.shared.models.Training
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class TrainingOverviewViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val training = Training(
        title = "Training title",
        description = "Training description can be long aisuhlkjbfl dsfh lkj ;foasihfl kjhlfkjh lakjfhalskfhlk jgdfl jkgdfkjhsd"
    )

}