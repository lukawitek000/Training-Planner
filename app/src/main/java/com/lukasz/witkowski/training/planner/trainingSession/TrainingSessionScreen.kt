package com.lukasz.witkowski.training.planner.trainingSession

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionState
import com.lukasz.witkowski.training.planner.ui.components.LoadingScreen

@Composable
fun TrainingSessionScreen(
    modifier: Modifier = Modifier,
    viewModel: TrainingSessionViewModel,
    navigateBack: () -> Unit
) {

    val trainingSessionState by viewModel.trainingSessionState.collectAsState()
    val time by viewModel.timer.collectAsState()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            Row() {
                FloatingActionButton(onClick = { viewModel.skip() }) {
                    Text(text = "Skip")
                }
                FloatingActionButton(onClick = { viewModel.completed() }) {
                    Text(text = "Complete")
                }
            }
        }
    ) {
        when (trainingSessionState) {
            is TrainingSessionState.ExerciseState -> TrainingExerciseScreen(
                exercise = (trainingSessionState as TrainingSessionState.ExerciseState).exercise
            )
            is TrainingSessionState.RestTimeState -> RestTimeScreen(
                restTime = time
            )
            is TrainingSessionState.SummaryState -> TrainingSessionSummaryScreen()
            else -> LoadingScreen(Modifier.fillMaxSize())
        }
    }
}

