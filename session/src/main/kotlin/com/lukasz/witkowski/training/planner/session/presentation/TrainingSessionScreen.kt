package com.lukasz.witkowski.training.planner.session.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens.spaceMd)
    ) {
        TrainingSessionContent(
            trainingSessionState = trainingSessionState,
            time = time,
            onRepsChanged = viewModel::onRepsChanged,
            onWeightChanged = viewModel::onWeightChanged,
            onHoldDurationChanged = viewModel::onHoldDurationChanged,
            onToggleHoldTimer = viewModel::toggleHoldTimer,
            onResetHoldTimer = viewModel::resetHoldTimer,
            onCompleteSet = viewModel::completed,
            onSkipSet = viewModel::skip,
            onSkipExercise = viewModel::skipExercise,
            onAddExtraSet = viewModel::addExtraSet,
            onFeedbackSelected = viewModel::onFeedbackSelected,
            onStartNextSet = viewModel::completed,
            onSkipRest = viewModel::skip,
            onSummaryRpeSelected = viewModel::onSummaryRpeSelected,
            onSummaryNotesChanged = viewModel::onSummaryNotesChanged,
            onSaveWorkout = {
                viewModel.saveStatistics()
                navigateBack()
            }
        )
    }
}

@Composable
fun TrainingSessionContent(
    trainingSessionState: TrainingSessionState,
    time: Time,
    onRepsChanged: (Int) -> Unit,
    onWeightChanged: (Double) -> Unit,
    onHoldDurationChanged: (Duration) -> Unit,
    onToggleHoldTimer: () -> Unit,
    onResetHoldTimer: () -> Unit,
    onCompleteSet: () -> Unit,
    onSkipSet: () -> Unit,
    onSkipExercise: () -> Unit,
    onAddExtraSet: () -> Unit,
    onFeedbackSelected: (SetEffort) -> Unit,
    onStartNextSet: () -> Unit,
    onSkipRest: () -> Unit,
    onSummaryRpeSelected: (SessionRpe) -> Unit,
    onSummaryNotesChanged: (String) -> Unit,
    onSaveWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (trainingSessionState) {
            is TrainingSessionState.ExerciseState -> {
                TrainingExerciseScreen(
                    state = trainingSessionState,
                    onRepsChanged = onRepsChanged,
                    onWeightChanged = onWeightChanged,
                    onHoldDurationChanged = onHoldDurationChanged,
                    onToggleHoldTimer = onToggleHoldTimer,
                    onResetHoldTimer = onResetHoldTimer,
                    onCompleteSet = onCompleteSet,
                    onSkipSet = onSkipSet,
                    onSkipExercise = onSkipExercise,
                    onAddExtraSet = onAddExtraSet
                )
            }

            is TrainingSessionState.RestTimeState -> {
                RestTimeScreen(
                    state = trainingSessionState,
                    timeLeft = time,
                    totalTime = trainingSessionState.time,
                    onFeedbackSelected = onFeedbackSelected,
                    onStartNextSet = onStartNextSet,
                    onSkipRest = onSkipRest
                )
            }

            is TrainingSessionState.SummaryState -> {
                TrainingSessionSummaryScreen(
                    state = trainingSessionState,
                    onRpeSelected = onSummaryRpeSelected,
                    onNotesChanged = onSummaryNotesChanged,
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
            time = Time.ZERO,
            onRepsChanged = {},
            onWeightChanged = {},
            onHoldDurationChanged = {},
            onToggleHoldTimer = {},
            onResetHoldTimer = {},
            onCompleteSet = {},
            onSkipSet = {},
            onSkipExercise = {},
            onAddExtraSet = {},
            onFeedbackSelected = {},
            onStartNextSet = {},
            onSkipRest = {},
            onSummaryRpeSelected = {},
            onSummaryNotesChanged = {},
            onSaveWorkout = {}
        )
    }
}
