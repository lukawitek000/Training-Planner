package com.lukasz.witkowski.training.planner.session.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.lukasz.witkowski.training.planner.session.R
import com.lukasz.witkowski.training.planner.session.presentation.models.TrainingSessionUiState
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.loading.AppLoadingState

/**
 * Main coordinator screen for the workout session.
 * Switches between [TrainingExerciseScreen], [RestTimeScreen], and [TrainingSessionSummaryScreen].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingSessionScreen(
    viewModel: TrainingSessionViewModel,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val trainingSessionState by viewModel.trainingSessionState.collectAsState()
    var showExitConfirmationDialog by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = trainingSessionState !is TrainingSessionUiState.Summary) {
        showExitConfirmationDialog = !showExitConfirmationDialog
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        TrainingSessionContent(
            trainingSessionUiState = trainingSessionState,
            onSessionIntent = viewModel::onIntent,
            onFinishWorkout = { navigateBack() },
            onEndClicked = { showExitConfirmationDialog = true }
        )
        if (showExitConfirmationDialog) {
            EndEarlySessionDialog(
                onConfirm = {
                    // TODO: Stop foreground service and cancel ongoing notifications before exiting session
                    viewModel.onIntent(SessionIntent.SessionEndEarly)
                    showExitConfirmationDialog = false
                    navigateBack()
                },
                onDismiss = {
                    showExitConfirmationDialog = false
                },
                modifier = Modifier
            )
        }
    }
}

@Composable
fun TrainingSessionContent(
    trainingSessionUiState: TrainingSessionUiState,
    onSessionIntent: (SessionIntent) -> Unit,
    onFinishWorkout: () -> Unit,
    onEndClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (trainingSessionUiState) {
            is TrainingSessionUiState.Exercise -> {
                TrainingExerciseScreen(
                    uiState = trainingSessionUiState,
                    onSessionIntent = onSessionIntent,
                    onEndClicked = onEndClicked
                )
            }

            is TrainingSessionUiState.RestTime -> {
                RestTimeScreen(
                    state = trainingSessionUiState.domainState,
                    onSessionIntent = onSessionIntent,
                    onEndClicked = onEndClicked
                )
            }

            is TrainingSessionUiState.Summary -> {
                TrainingSessionSummaryScreen(
                    uiState = trainingSessionUiState,
                    onSessionIntent = onSessionIntent,
                    onFinishWorkout = onFinishWorkout
                )
            }

            else -> {
                AppLoadingState(
                    title = stringResource(R.string.session_loading_title),
                    subtitle = stringResource(R.string.session_loading_subtitle)
                )
            }
        }
    }
}

@AppPreview
@Composable
private fun TrainingSessionContentLoadingPreview() {
    TrainingPlannerTheme {
        TrainingSessionContent(
            trainingSessionUiState = TrainingSessionUiState.Idle,
            onSessionIntent = {},
            onFinishWorkout = {},
            onEndClicked = {}
        )
    }
}
