package com.lukasz.witkowski.training.planner.statistics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lukasz.witkowski.training.planner.training.models.TrainingPlan
import com.lukasz.witkowski.training.planner.ui.components.LoadingScreen

@Composable
fun TrainingSessionScreen(
    modifier: Modifier = Modifier,
    viewModel: TrainingSessionViewModel,
    navigateBack: () -> Unit
) {

    val trainingSessionState by viewModel.trainingSessionState.collectAsState()
    Scaffold(modifier = modifier.fillMaxSize()) {
        when (trainingSessionState) {
            is TrainingSessionState.Idle -> LoadingTrainingPlan()
            is TrainingSessionState.TrainingPlanLoadedState -> LoadedTrainingPlanOverview(
                trainingPlan = (trainingSessionState as TrainingSessionState.TrainingPlanLoadedState).trainingPlan
            )
        }
    }
}

@Composable
fun LoadingTrainingPlan(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LoadingScreen()
    }
}

@Composable
fun LoadedTrainingPlanOverview(
    modifier: Modifier = Modifier,
    trainingPlan: TrainingPlan
) {
    Text(text = trainingPlan.toString())
}