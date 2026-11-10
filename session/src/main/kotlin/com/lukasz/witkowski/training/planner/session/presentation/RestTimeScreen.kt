package com.lukasz.witkowski.training.planner.session.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.lukasz.witkowski.training.planner.session.R
import com.lukasz.witkowski.training.planner.session.domain.models.SetEffort
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExercise
import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExerciseConfiguration
import com.lukasz.witkowski.training.planner.session.domain.session.models.TrainingExerciseSnapshot
import com.lukasz.witkowski.training.planner.session.domain.session.queue.ExerciseQueueProgress
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerState
import com.lukasz.witkowski.training.planner.session.presentation.components.AppFeedbackChips
import com.lukasz.witkowski.training.planner.session.presentation.components.TargetMetricsOverview
import com.lukasz.witkowski.training.planner.shared.theme.Dimens
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.button.AppPrimaryButton
import com.lukasz.witkowski.training.planner.shared.ui.card.AppCard
import com.lukasz.witkowski.training.planner.shared.ui.timer.AppCircularTimer
import com.lukasz.witkowski.training.planner.training.domain.ExerciseCategoryName
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private data class FeedbackChipOption(val effort: SetEffort, val label: String)

/**
 * Rest interval screen displaying circular countdown ring, set difficulty feedback chips,
 * and up-next exercise preview.
 */
@Composable
fun RestTimeScreen(
    state: TrainingSessionState.RestTimeState,
    onSessionIntent: (SessionIntent) -> Unit,
    onEndClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val feedbackChipOptions = listOf(
        FeedbackChipOption(SetEffort.EASY, stringResource(R.string.session_feedback_easy)),
        FeedbackChipOption(SetEffort.MODERATE, stringResource(R.string.session_feedback_good)),
        FeedbackChipOption(SetEffort.HARD, stringResource(R.string.session_feedback_hard)),
        FeedbackChipOption(SetEffort.MAXIMAL, stringResource(R.string.session_feedback_limit))
    )
    val remainingTime = (state.timerState as? TimerState.Running)?.remaining ?: Duration.ZERO
    val progress = if (state.totalRestTime.inWholeMilliseconds > 0) {
        (remainingTime/ state.totalRestTime).toFloat()
    } else {
        0f
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = Dimens.spaceMd, end = Dimens.spaceMd, bottom = Dimens.spaceMd),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceMd)
    ) {
        RestHeader(
            onEndClicked = onEndClicked
        )
        // TODO: Play 3-2-1 audio beeps and trigger vibration haptic pulses as rest interval finishes
        AppCircularTimer(
            timeFormatted = formatRemainingTime(remainingTime),
            progress = progress,
            label = stringResource(R.string.session_rest_remaining)
        )
        SetFeedbackForm(feedbackChipOptions, state, onSessionIntent)

        NextExercisePreview(state)

        Spacer(modifier = Modifier.height(Dimens.spaceSm))

        AppPrimaryButton(
            text = stringResource(R.string.session_start_next_set_now),
            leadingIcon = Icons.Default.PlayArrow,
            onClick = { onSessionIntent(SessionIntent.RestSkipped) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun NextExercisePreview(state: TrainingSessionState.RestTimeState) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceMd),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppExerciseImage(
                imageIds = state.nextExercise.snapshot.imageIds,
                contentDescription = state.nextExercise.snapshot.name,
                modifier = Modifier.size(Dimens.listItemThumbnailSize)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
            ) {
                Text(
                    text = stringResource(R.string.session_up_next),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = state.nextExercise.snapshot.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // TODO generate progress for the next exercise
                    state.nextSetQueueProgress?.let { nextProgress ->
                        Text(
                            text = stringResource(
                                R.string.session_set_progress_format,
                                nextProgress.currentExerciseSet,
                                nextProgress.currentExerciseTotalSets
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    val nextExercise = state.nextExercise
                    TargetMetricsOverview(nextExercise)
                }
            }
        }
    }
}

@Composable
private fun RestHeader(
    onEndClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .padding(horizontal = Dimens.spaceXl)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = Dimens.spaceMd, vertical = Dimens.spaceXs)
                .align(Alignment.Center)
        ) {
            Text(
                text = stringResource(R.string.session_rest_interval),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        EndEarlySessionButton(
            onEndClicked = onEndClicked,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }

}

@Composable
private fun SetFeedbackForm(
    feedbackChipOptions: List<FeedbackChipOption>,
    state: TrainingSessionState.RestTimeState,
    onSessionIntent: (SessionIntent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
    ) {
        Text(
            text = stringResource(R.string.session_feedback_prompt),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        AppFeedbackChips(
            options = feedbackChipOptions,
            selectedOption = feedbackChipOptions.firstOrNull {
                it.effort == state.selectedFeedback
            },
            onOptionSelected = { onSessionIntent(SessionIntent.SetFeedbackSelected(it.effort)) },
            labelProvider = { it.label }
        )
    }
}

@AppPreview
@Composable
private fun RestTimeScreenPreview() {
    val dummyConfig = SessionExerciseConfiguration(
        reps = 12,
        weightInKg = 50.0,
        holdTime = null
    )
    val dummySnapshot = TrainingExerciseSnapshot(
        id = TrainingExerciseId.create(),
        name = "Incline Dumbbell Press",
        description = "Press dumbbells at incline",
        categories = setOf(ExerciseCategoryName("Chest")),
        imageIds = emptyList(),
        plannedConfig = dummyConfig
    )
    val nextSessionExercise = SessionExercise(
        snapshot = dummySnapshot,
        currentConfig = dummyConfig,
        restTime = 60.seconds
    )
    val nextProgress = ExerciseQueueProgress(
        remainingExerciseSets = 1,
        currentExerciseTotalSets = 3,
        totalExercises = 4,
        remainingExercises = 1,
        remainingSetsInWorkout = 6,
        nextSetQueueProgress = null
    )
    val state = TrainingSessionState.RestTimeState(
        nextExercise = nextSessionExercise,
        timerState = TimerState.Running(42.seconds),
        totalRestTime = 60.seconds,
        nextSetQueueProgress = nextProgress,
        selectedFeedback = null
    )

    TrainingPlannerTheme {
        RestTimeScreen(
            state = state,
            onSessionIntent = {},
            onEndClicked = {}
        )
    }
}
