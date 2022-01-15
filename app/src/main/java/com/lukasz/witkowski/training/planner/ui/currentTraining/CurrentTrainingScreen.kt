package com.lukasz.witkowski.training.planner.ui.currentTraining

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CurrentTrainingScreen(
    modifier: Modifier = Modifier,
    viewModel: CurrentTrainingViewModel,
    navigateBack: () -> Unit
) {
    Scaffold(modifier = modifier.fillMaxSize()) {
        Text(text = "Training id ${viewModel.trainingId}")
    }

}