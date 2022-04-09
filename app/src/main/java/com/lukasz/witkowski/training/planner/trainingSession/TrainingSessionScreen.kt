package com.lukasz.witkowski.training.planner.trainingSession

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionState
import com.lukasz.witkowski.training.planner.trainingSession.components.FabTextWithIcon
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
        Column(
            modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .weight(0.9f)) {
                when (trainingSessionState) {
                    is TrainingSessionState.ExerciseState -> TrainingExerciseScreen(
                        exercise = trainingSessionState.exercise!!,
                        remainingTime = time,
                        start = { viewModel.startTimer() },
                        pause = { viewModel.pauseTimer() },
                        reset = { viewModel.resetTimer() },
                        isTimerRunning = isTimerRunning
                    )
                    is TrainingSessionState.RestTimeState -> RestTimeScreen(
                        timeLeft = time,
                        totalTime = trainingSessionState.time,
                        nextExercise = trainingSessionState.exercise!!
                    )
                    is TrainingSessionState.SummaryState -> TrainingSessionSummaryScreen(
                        statistics = (trainingSessionState as TrainingSessionState.SummaryState).statistics
                    )
                    else -> LoadingScreen(Modifier.fillMaxSize())
                }
            }

            AnimatedVisibility(visible = !(trainingSessionState is TrainingSessionState.SummaryState)) {
                CompletedAndSkipFabs(
                    modifier = Modifier.weight(0.1f),
                    completed = { viewModel.completed() },
                    skip = { viewModel.skip() },
                    isCompletedFabVisible = trainingSessionState is TrainingSessionState.ExerciseState
                )
            }
        }
    }
}

@Composable
fun CompletedAndSkipFabs(
    modifier: Modifier = Modifier,
    completed: () -> Unit,
    skip: () -> Unit,
    isCompletedFabVisible: Boolean
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        FabTextWithIcon(
            text = stringResource(id = R.string.skip),
            imageVector = Icons.Filled.SkipNext,
            onClick = skip
        )
        AnimatedVisibility(visible = isCompletedFabVisible) {
            FabTextWithIcon(
                modifier = Modifier.padding(start = 32.dp),
                text = stringResource(id = R.string.completed),
                imageVector = Icons.Filled.Check,
                onClick = completed
            )
        }
    }
}
