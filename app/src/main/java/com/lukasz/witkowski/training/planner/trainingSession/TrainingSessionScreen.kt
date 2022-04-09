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
    val isTimerRunning by viewModel.isRunning.collectAsState()

    Scaffold(
        modifier = modifier,
    ) {
        when (trainingSessionState) {
            is TrainingSessionState.ExerciseState -> TrainingExerciseScreen(
                exercise = trainingSessionState.exercise!!,
                remainingTime = time,
                start = { viewModel.startTimer() },
                pause = { viewModel.pauseTimer() },
                reset = { viewModel.resetTimer() },
                isTimerRunning = isTimerRunning,
                skip = { viewModel.skip() },
                completed = { viewModel.completed() }
            )
            is TrainingSessionState.RestTimeState -> RestTimeScreen(
                timeLeft = time,
                totalTime = trainingSessionState.time,
                nextExercise = trainingSessionState.exercise!!,
                skip = { viewModel.skip() }
            )
            is TrainingSessionState.SummaryState -> TrainingSessionSummaryScreen(
                statistics = (trainingSessionState as TrainingSessionState.SummaryState).statistics
            )
            else -> LoadingScreen(Modifier.fillMaxSize())
        }
    }
}

