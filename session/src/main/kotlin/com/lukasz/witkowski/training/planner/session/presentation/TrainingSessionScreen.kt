package com.lukasz.witkowski.training.planner.session.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.lukasz.witkowski.training.planner.session.R
import com.lukasz.witkowski.training.planner.session.presentation.models.TrainingSessionPhaseKey
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

    BackHandler(enabled = true) {
        if (trainingSessionState is TrainingSessionUiState.Summary) {
            viewModel.onIntent(SessionIntent.FinishWorkout)
            navigateBack()
        } else {
            showExitConfirmationDialog = !showExitConfirmationDialog
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        TrainingSessionContent(
            trainingSessionUiState = trainingSessionState,
            onSessionIntent = viewModel::onIntent,
            onFinishWorkout = {
                viewModel.onIntent(SessionIntent.FinishWorkout)
                navigateBack()
            },
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
        AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            targetState = trainingSessionUiState.phaseKey,
            transitionSpec = {
                slideInHorizontally { it } + fadeIn() togetherWith
                    slideOutHorizontally { -it } + fadeOut()
            },
            label = "TrainingSessionPhasesAnimation"
        ) { phaseKey ->
            when (phaseKey) {
                is TrainingSessionPhaseKey.Exercise -> {
                    rememberPhaseState<TrainingSessionUiState.Exercise>(
                        currentState = trainingSessionUiState,
                        expectedKey = phaseKey
                    )?.let { uiState ->
                        TrainingExerciseScreen(
                            uiState = uiState,
                            onSessionIntent = onSessionIntent,
                            onEndClicked = onEndClicked
                        )
                    }
                }

                TrainingSessionPhaseKey.RestTime -> {
                    rememberPhaseState<TrainingSessionUiState.RestTime>(
                        currentState = trainingSessionUiState,
                        expectedKey = phaseKey
                    )?.let { uiState ->
                        RestTimeScreen(
                            state = uiState.domainState,
                            onSessionIntent = onSessionIntent,
                            onEndClicked = onEndClicked
                        )
                    }
                }

                TrainingSessionPhaseKey.Summary -> {
                    rememberPhaseState<TrainingSessionUiState.Summary>(
                        currentState = trainingSessionUiState,
                        expectedKey = phaseKey
                    )?.let { uiState ->
                        TrainingSessionSummaryScreen(
                            uiState = uiState,
                            onSessionIntent = onSessionIntent,
                            onFinishWorkout = onFinishWorkout
                        )
                    }
                }

                TrainingSessionPhaseKey.Loading -> {
                    AppLoadingState(
                        title = stringResource(R.string.session_loading_title),
                        subtitle = stringResource(R.string.session_loading_subtitle)
                    )
                }
            }
        }
    }
}

@Composable
inline fun <reified T : TrainingSessionUiState> rememberPhaseState(
    currentState: TrainingSessionUiState,
    expectedKey: TrainingSessionPhaseKey
): T? {
    var cached by remember(expectedKey) {
        mutableStateOf(
            if (currentState is T && currentState.phaseKey == expectedKey) currentState else null
        )
    }
    if (currentState is T && currentState.phaseKey == expectedKey) {
        cached = currentState
    }
    return cached
}

@AppPreview
@Composable
private fun TrainingSessionContentLoadingPreview() {
    TrainingPlannerTheme {
        TrainingSessionContent(
            trainingSessionUiState = TrainingSessionUiState.Loading,
            onSessionIntent = {},
            onFinishWorkout = {},
            onEndClicked = {}
        )
    }
}
