package com.lukasz.witkowski.training.planner.statistics

import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.lukasz.witkowski.training.planner.training.models.TrainingPlan

@Composable
fun TrainingSessionScreen(
    modifier: Modifier = Modifier,
    viewModel: TrainingSessionViewModel,
    navigateBack: () -> Unit
) {

    val trainingPlan by viewModel.trainingPlan.collectAsState()
    Scaffold(modifier = modifier) {
        Text(text = trainingPlan.toString())
    }



}