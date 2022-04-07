package com.lukasz.witkowski.training.planner.statistics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionState
import com.lukasz.witkowski.training.planner.training.presentation.TrainingExercise
import com.lukasz.witkowski.training.planner.ui.components.LoadingScreen

@Composable
fun TrainingSessionScreen(
    modifier: Modifier = Modifier,
    viewModel: TrainingSessionViewModel,
    navigateBack: () -> Unit
) {

    val trainingSessionState by viewModel.trainingSessionState.collectAsState()
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
                restTime = trainingSessionState.time
            )
            is TrainingSessionState.SummaryState -> TrainingSessionSummaryScreen()
            else -> LoadingScreen(Modifier.fillMaxSize())
        }
    }
}

@Composable
fun TrainingSessionSummaryScreen() {
    Text(text = "Summary")
}

@Composable
fun RestTimeScreen(
    modifier: Modifier = Modifier,
    restTime: Long
) {
    Column {
        Text(text = "Rest time")
        Text(text = restTime.toString())
    }

}

@Composable
fun TrainingExerciseScreen(
    modifier: Modifier = Modifier,
    exercise: TrainingExercise
) {
    Text(text = exercise.toString())
}
