package com.lukasz.witkowski.training.planner.session.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.lukasz.witkowski.training.planner.session.R
import com.lukasz.witkowski.training.planner.session.domain.models.SessionRpe
import com.lukasz.witkowski.training.planner.session.domain.models.SetEffort
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.shared.theme.Dimens
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.dialogs.AppConfirmationDialog
import com.lukasz.witkowski.training.planner.shared.ui.loading.AppLoadingState
import kotlin.time.Duration

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
    var showExitConfirmationDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = trainingSessionState !is TrainingSessionState.SummaryState) {
        showExitConfirmationDialog = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        TrainingSessionContent(
            trainingSessionState = trainingSessionState,
            onSessionIntent = viewModel::onIntent,
            onSaveWorkout = {
                viewModel.saveStatistics()
                navigateBack()
            }
        )
    }

    if (showExitConfirmationDialog) {
        AppConfirmationDialog(
            title = stringResource(R.string.session_exit_dialog_title),
            message = stringResource(R.string.session_exit_dialog_message),
            confirmText = stringResource(R.string.session_exit_dialog_confirm),
            dismissText = stringResource(R.string.session_exit_dialog_dismiss),
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
}

@Composable
fun TrainingSessionContent(
    trainingSessionState: TrainingSessionState,
    onSessionIntent: (SessionIntent) -> Unit,
    onSaveWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (trainingSessionState) {
            is TrainingSessionState.ExerciseState -> {
                TrainingExerciseScreen(
                    state = trainingSessionState,
                    onSessionIntent = onSessionIntent
                )
            }

            is TrainingSessionState.RestTimeState -> {
                RestTimeScreen(
                    state = trainingSessionState,
                    timeLeft = Duration.ZERO,
                    totalTime = trainingSessionState.totalRestTime,
                    onSessionIntent = onSessionIntent,
                )
            }

            is TrainingSessionState.SummaryState -> {
                TrainingSessionSummaryScreen(
                    state = trainingSessionState,
                    onSessionIntent = onSessionIntent,
                    onSaveWorkout = onSaveWorkout
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
            trainingSessionState = TrainingSessionState.IdleState,
            onSessionIntent = {},
            onSaveWorkout = {}
        )
    }
}
