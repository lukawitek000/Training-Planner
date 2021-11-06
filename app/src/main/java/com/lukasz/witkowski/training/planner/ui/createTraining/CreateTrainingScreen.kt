package com.lukasz.witkowski.training.planner.ui.createTraining

import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lukasz.witkowski.training.planner.ui.createExercise.CreateExerciseViewModel

@Composable
fun CreateTrainingScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateTrainingViewModel,
    navigateBack: () -> Unit
) {
    Scaffold() {
        Text(text = "Create training")
    }
}