package com.lukasz.witkowski.training.planner.trainingSession

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
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
    ) {
        when (trainingSessionState) {
            is TrainingSessionState.ExerciseState -> TrainingExerciseScreen(
                exercise = trainingSessionState.exercise!!
            )
            is TrainingSessionState.RestTimeState -> RestTimeScreen(
                timeLeft = time,
                totalTime = (trainingSessionState as TrainingSessionState.RestTimeState).restTime,
                nextExercise = trainingSessionState.exercise!!,
                skip = {}
            )
            is TrainingSessionState.SummaryState -> TrainingSessionSummaryScreen()
            else -> LoadingScreen(Modifier.fillMaxSize())
        }
    }
}

