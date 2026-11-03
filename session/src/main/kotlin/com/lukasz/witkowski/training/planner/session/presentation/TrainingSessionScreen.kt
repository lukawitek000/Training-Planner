package com.lukasz.witkowski.training.planner.session.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.lukasz.witkowski.training.planner.session.presentation.TrainingSessionState
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.ui.dialogs.AppConfirmationDialog
import com.lukasz.witkowski.training.planner.shared.ui.loading.AppLoadingState

/**
 * Main coordinator screen for the workout session.
 * Switches between [TrainingExerciseScreen], [RestTimeScreen], and [TrainingSessionSummaryScreen].
 */
@Composable
fun TrainingSessionScreen(
    viewModel: TrainingSessionViewModel,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val trainingSessionState by viewModel.trainingSessionState.collectAsState()
    val time by viewModel.time.collectAsState()
    var showExitConfirmationDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = trainingSessionState !is TrainingSessionState.SummaryState) {
        showExitConfirmationDialog = true
    }

    if (showExitConfirmationDialog) {
        AppConfirmationDialog(
            title = "Exit Workout Session?",
            message =
            "Are you sure you want to exit? " +
                "Your progress in this workout will not be saved.",
            confirmText = "Exit Workout",
            dismissText = "Continue",
            isDestructive = true,
            onConfirm = {
                // TODO: Stop foreground service and cancel ongoing notifications before exiting session
                showExitConfirmationDialog = false
                navigateBack()
            },
            onDismiss = {
                showExitConfirmationDialog = false
            }
        )
    }

    TrainingPlannerTheme2 {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (val state = trainingSessionState) {
                    is TrainingSessionState.ExerciseState -> {
                        TrainingExerciseScreen(
                            state = state,
                            onCompleteSet = { reps, weight, time ->
                                viewModel.completed(
                                    actualReps = reps,
                                    actualWeightInKg = weight,
                                    actualTime = time
                                )
                            },
                            onSkipSet = { viewModel.skip() },
                            onSkipExercise = { viewModel.skipExercise() },
                            onAddExtraSet = { viewModel.addExtraSet() }
                        )
                    }

                    is TrainingSessionState.RestTimeState -> {
                        RestTimeScreen(
                            state = state,
                            timeLeft = time,
                            totalTime = state.time,
                            onStartNextSet = { viewModel.completed() },
                            onSkipRest = { viewModel.skip() }
                        )
                    }

                    is TrainingSessionState.SummaryState -> {
                        TrainingSessionSummaryScreen(
                            statistics = state.statistics,
                            trainingPlan = state.trainingPlan,
                            onSaveWorkout = { note, rpe ->
                                viewModel.saveStatistics(note, rpe)
                                navigateBack()
                            }
                        )
                    }

                    else -> {
                        AppLoadingState(
                            title = "Loading Session...",
                            subtitle = "Preparing workout plan progression and exercise metrics."
                        )
                    }
                }
            }
        }
    }
}
