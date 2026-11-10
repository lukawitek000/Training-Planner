package com.lukasz.witkowski.training.planner.session.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.presentation.PREVIEW_PUSH_UP_IMAGES
import com.lukasz.witkowski.training.planner.image.ImageReference
import com.lukasz.witkowski.training.planner.image.presentation.ImageGallery
import com.lukasz.witkowski.training.planner.session.R
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExercise
import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExerciseConfiguration
import com.lukasz.witkowski.training.planner.session.domain.session.models.TrainingExerciseSnapshot
import com.lukasz.witkowski.training.planner.session.domain.session.queue.ExerciseQueueProgress
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerState
import com.lukasz.witkowski.training.planner.session.presentation.components.AppIntStepper
import com.lukasz.witkowski.training.planner.session.presentation.components.AppStepper
import com.lukasz.witkowski.training.planner.session.presentation.components.TargetMetricsOverview
import com.lukasz.witkowski.training.planner.session.presentation.models.TrainingSessionUiState
import com.lukasz.witkowski.training.planner.shared.theme.Dimens
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.button.AppButton
import com.lukasz.witkowski.training.planner.shared.ui.button.AppPrimaryButton
import com.lukasz.witkowski.training.planner.shared.ui.button.AppSecondaryButton
import com.lukasz.witkowski.training.planner.shared.ui.button.AppTertiaryButton
import com.lukasz.witkowski.training.planner.shared.ui.card.AppCard
import com.lukasz.witkowski.training.planner.shared.ui.chips.AppCategoryPill
import com.lukasz.witkowski.training.planner.shared.ui.loading.AppLoadingIndicator
import com.lukasz.witkowski.training.planner.shared.ui.loading.AppLoadingState
import com.lukasz.witkowski.training.planner.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.training.domain.ExerciseCategoryName
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Active workout session screen displaying current exercise, offline image illustration,
 * set progression, reps/hold-time/weight steppers, and set completion controls.
 */
@Composable
fun TrainingExerciseScreen(
    uiState: TrainingSessionUiState.Exercise,
    onSessionIntent: (SessionIntent) -> Unit,
    onEndClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val state = uiState.domainState

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(start = Dimens.spaceMd, end = Dimens.spaceMd, bottom = Dimens.spaceMd),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceMd)
    ) {
        ExerciseHeader(
            exerciseName = state.sessionExercise.snapshot.name,
            categories = state.sessionExercise.snapshot.categories.map { it.name },
            exerciseIndex = state.progress.exerciseIndex,
            totalExercises = state.progress.totalExercises,
            onEndClicked = onEndClicked
        )

        ExerciseImages(
            images = uiState.imagesReferences,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.heroImageHeightCompact),
        )

        SetsProgressStrip(
            currentSet = state.progress.currentExerciseSet,
            totalSets = state.progress.currentExerciseTotalSets
        )

        ActiveSetCard(state, onSessionIntent)

        ExerciseSetsModification(onSessionIntent)

        Spacer(modifier = Modifier.height(Dimens.spaceMd))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceMd)
        ) {
            AppPrimaryButton(
                text = stringResource(R.string.session_complete_set),
                leadingIcon = Icons.Default.Check,
                onClick = { onSessionIntent(SessionIntent.SetCompleted) },
                modifier = Modifier.fillMaxWidth()
            )

            AppSecondaryButton(
                text = stringResource(R.string.session_skip_set),
                leadingIcon = Icons.Default.SkipNext,
                onClick = { onSessionIntent(SessionIntent.SetSkipped) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ActiveSetCard(
    state: TrainingSessionState.ExerciseState,
    onSessionIntent: (SessionIntent) -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceMd),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceMd)
        ) {
            ActiveSetHeader(state)

            if (state.sessionExercise.currentConfig.isIsometric) {
                IsometricHoldControls(
                    state = state,
                    onSessionIntent = onSessionIntent
                )
            } else {
                AppIntStepper(
                    value = state.sessionExercise.currentConfig.reps,
                    onValueChange = { onSessionIntent(SessionIntent.RepsChanged(it)) },
                    label = stringResource(R.string.session_reps_label),
                    minValue = 0
                )
            }

            AppStepper(
                value = state.sessionExercise.currentConfig.weightInKg ?: 0.0,
                onValueChange = { onSessionIntent(SessionIntent.WeightChanged(it)) },
                step = 2.5,
                label = stringResource(R.string.session_kg_label),
                minValue = 0.0
            )
        }
    }
}

@Composable
private fun ActiveSetHeader(state: TrainingSessionState.ExerciseState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(
                R.string.session_active_set_format,
                state.progress.currentExerciseSet
            ),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        TargetMetricsOverview(state.sessionExercise)
    }
}

@Composable
private fun ExerciseSetsModification(onSessionIntent: (SessionIntent) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppButton(
            text = stringResource(R.string.session_remove_set),
            leadingIcon = Icons.Default.Remove,
            onClick = { onSessionIntent(SessionIntent.SetRemoved) },
            modifier = Modifier.weight(1f),
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
        )

        AppTertiaryButton(
            text = stringResource(R.string.session_add_extra_set),
            leadingIcon = Icons.Default.Add,
            onClick = { onSessionIntent(SessionIntent.SetAdded) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun IsometricHoldControls(
    state: TrainingSessionState.ExerciseState,
    onSessionIntent: (SessionIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val holdTimeState =
        HoldTimeState.create(state.timerState, state.sessionExercise.currentConfig.holdTime)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(holdTimeState.backgroundColor)
                .padding(horizontal = Dimens.spaceMd, vertical = Dimens.spaceXl),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = holdTimeState.iconsColor,
                modifier = Modifier
                    .size(Dimens.iconSize30)
                    .weight(0.25f)
            )
            TimerClock(
                holdTimeState.remainingTime,
                holdTimeState.timerColor,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier.weight(0.25f),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { onSessionIntent(holdTimeState.sessionIntentFactory()) },
                    modifier = Modifier,
                    colors = IconButtonColors(
                        contentColor = holdTimeState.iconsColor,
                        disabledContentColor = holdTimeState.iconsColor.copy(alpha = 0.5f),
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    )
                ) {
                    Icon(
                        imageVector = holdTimeState.icon,
                        contentDescription = stringResource(R.string.session_start_hold_timer),
                        tint = holdTimeState.iconsColor,
                        modifier = Modifier.size(Dimens.iconSize30)
                    )
                }
            }
        }

        AppIntStepper(
            value = state.sessionExercise.currentConfig.holdTime?.inWholeSeconds?.toInt() ?: 0,
            onValueChange = { onSessionIntent(SessionIntent.HoldTimeChanged(it.seconds)) },
            step = 5,
            label = stringResource(R.string.session_sec_hold_label),
            minValue = 0,
            enabled = state.timerState is TimerState.Ready || state.timerState is TimerState.NotSet
        )
    }
}

@Composable
private fun TimerClock(
    remainingTime: Duration,
    timerColor: Color,
    modifier: Modifier = Modifier
) {
    val style = MaterialTheme.typography.headlineLarge.copy(
        fontWeight = FontWeight.Bold,
    )
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.session_timer_negative),
            textAlign = TextAlign.End,
            style = style,
            color = timerColor,
            modifier = Modifier.alpha(if (remainingTime.isNegative()) 1f else 0f)
        )
        Text(
            text = formatRemainingTime(remainingTime),
            style = style,
            color = timerColor,
            modifier = Modifier,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.session_timer_negative),
            style = style,
            textAlign = TextAlign.Start,
            modifier = Modifier.alpha(0f)
        )
    }
}

private data class HoldTimeState(
    val remainingTime: Duration,
    val iconsColor: Color,
    val timerColor: Color,
    val backgroundColor: Color,
    val icon: ImageVector,
    val sessionIntentFactory: () -> SessionIntent,
) {
    companion object {
        @Composable
        fun create(timerState: TimerState, initialHoldDuration: Duration?): HoldTimeState {
            val remainingTime = when (timerState) {
                is TimerState.Ready -> initialHoldDuration ?: Duration.ZERO
                is TimerState.NotSet -> Duration.ZERO
                is TimerState.Running -> timerState.remaining
                is TimerState.OverflowRunning -> timerState.overflow
                is TimerState.Finished -> Duration.ZERO
            }
            val icon = when (timerState) {
                is TimerState.Ready -> Icons.Default.PlayArrow
                is TimerState.NotSet -> Icons.Default.Error
                else -> Icons.Default.Refresh
            }
            return HoldTimeState(
                remainingTime = remainingTime,
                iconsColor = if (remainingTime < Duration.ZERO) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.primary
                },
                timerColor = if (remainingTime < Duration.ZERO) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                backgroundColor = if (remainingTime < Duration.ZERO) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                },
                icon = icon,
                sessionIntentFactory = {
                    when (timerState) {
                        is TimerState.Ready -> SessionIntent.StartTimer
                        else -> SessionIntent.ResetTimer
                    }
                }
            )
        }
    }
}

@Composable
private fun ExerciseHeader(
    exerciseName: String,
    categories: List<String>,
    exerciseIndex: Int,
    totalExercises: Int,
    onEndClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(
                    R.string.session_exercise_progress_format,
                    exerciseIndex,
                    totalExercises
                ),
                textAlign = TextAlign.Left,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            EndEarlySessionButton(
                onEndClicked = onEndClicked,
            )
        }

        Text(
            text = exerciseName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(Dimens.spaceSm))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
        ) {
            categories.forEach { category ->
                AppCategoryPill(text = category)
            }
        }
    }
}

@Composable
private fun SetsProgressStrip(currentSet: Int, totalSets: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (setIndex in 1..totalSets) {
            val isCompleted = setIndex < currentSet
            val isCurrent = setIndex == currentSet

            val backgroundColor = when {
                isCompleted -> MaterialTheme.colorScheme.primaryContainer
                isCurrent -> MaterialTheme.colorScheme.surfaceContainerHighest
                else -> MaterialTheme.colorScheme.surfaceContainer
            }
            val borderColor = when {
                isCurrent -> MaterialTheme.colorScheme.primary
                isCompleted -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.outlineVariant
            }
            val textColor = when {
                isCompleted -> MaterialTheme.colorScheme.onPrimaryContainer
                isCurrent -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outline
            }

            Box(
                modifier = Modifier
                    .size(Dimens.setChipSize)
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .border(
                        width = if (isCurrent) Dimens.mediumBorder else Dimens.thinBorder,
                        color = borderColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(
                            R.string.session_completed_set_cd,
                            setIndex
                        ),
                        tint = textColor,
                        modifier = Modifier.size(Dimens.iconSize16)
                    )
                } else {
                    Text(
                        text = setIndex.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseImages(
    images: ResultHandler<List<ImageReference>>,
    modifier: Modifier = Modifier,
) {
    when (images) {
        is ResultHandler.Loading -> {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AppLoadingIndicator(
                    modifier = Modifier,
                    spinnerSize = Dimens.smallSpinnerSize,
                    spinnerIconContainerSize = Dimens.smallSpinnerIconContainerSize
                )
                Spacer(Modifier.height(Dimens.spaceMd))
                Text(
                    text = "Loading exercise images",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        is ResultHandler.Success<List<ImageReference>> -> {
            if (images.value.isNotEmpty()) {
                ImageGallery(
                    images = images.value,
                    modifier = modifier,
                )
            }
        }

        is ResultHandler.Idle -> {}
        is ResultHandler.Error -> {}
    }
}

@AppPreview
@Composable
private fun TrainingExerciseScreenPreview() {
    val dummyConfig = SessionExerciseConfiguration(
        reps = 10,
        weightInKg = 85.0,
        holdTime = null
    )
    val dummySnapshot = TrainingExerciseSnapshot(
        id = TrainingExerciseId.create(),
        exerciseId = ExerciseId.create(),
        name = "Barbell Bench Press",
        description = "Lie on bench, press bar upwards",
        categories = setOf(ExerciseCategoryName("Chest")),
        imageIds = PREVIEW_PUSH_UP_IMAGES.map { it.imageId },
        plannedConfig = dummyConfig
    )
    val sessionExercise = SessionExercise(
        snapshot = dummySnapshot,
        currentConfig = dummyConfig,
        restTime = 90.seconds
    )
    val progress = ExerciseQueueProgress(
        remainingExerciseSets = 2,
        currentExerciseTotalSets = 4,
        totalExercises = 4,
        remainingExercises = 3,
        remainingSetsInWorkout = 10,
        nextSetQueueProgress = null,
    )
    val state = TrainingSessionState.ExerciseState(
        sessionExercise = sessionExercise,
        timerState = TimerState.NotSet,
        progress = progress
    )
    val uiState = TrainingSessionUiState.Exercise(
        state, ResultHandler.Success(PREVIEW_PUSH_UP_IMAGES)
    )

    TrainingPlannerTheme {
        TrainingExerciseScreen(
            uiState = uiState,
            onSessionIntent = {},
            onEndClicked = {}
        )
    }
}

@AppPreview
@Composable
private fun TrainingExerciseScreenHoldTimePreview() {
    val dummyConfig = SessionExerciseConfiguration(
        reps = 0,
        weightInKg = null,
        holdTime = 68.seconds
    )
    val dummySnapshot = TrainingExerciseSnapshot(
        id = TrainingExerciseId.create(),
        exerciseId = ExerciseId.create(),
        name = "Plank",
        description = "Hold a straight body position resting on forearms and toes",
        categories = setOf(
            ExerciseCategoryName("Core"),
            ExerciseCategoryName("Chest"),
            ExerciseCategoryName("Biceps")
        ),
        imageIds = emptyList(),
        plannedConfig = dummyConfig
    )
    val sessionExercise = SessionExercise(
        snapshot = dummySnapshot,
        currentConfig = dummyConfig,
        restTime = 45.seconds
    )
    val progress = ExerciseQueueProgress(
        remainingExerciseSets = 2,
        currentExerciseTotalSets = 3,
        totalExercises = 4,
        remainingExercises = 2,
        remainingSetsInWorkout = 6,
        nextSetQueueProgress = null
    )
    val state = TrainingSessionState.ExerciseState(
        sessionExercise = sessionExercise,
        timerState = TimerState.Ready(68.seconds),
        progress = progress
    )
    val uiState = TrainingSessionUiState.Exercise(
        domainState = state,
        imagesReferences = ResultHandler.Loading
    )

    TrainingPlannerTheme {
        TrainingExerciseScreen(
            uiState = uiState,
            onSessionIntent = {},
            onEndClicked = {}
        )
    }
}

@AppPreview
@Composable
private fun TrainingExerciseScreenNegativeHoldTimePreview() {
    val dummyConfig = SessionExerciseConfiguration(
        reps = 0,
        weightInKg = null,
        holdTime = 20.seconds
    )
    val dummySnapshot = TrainingExerciseSnapshot(
        id = TrainingExerciseId.create(),
        exerciseId = ExerciseId.create(),
        name = "Plank",
        description = "Hold a straight body position resting on forearms and toes",
        categories = setOf(ExerciseCategoryName("Core")),
        imageIds = emptyList(),
        plannedConfig = dummyConfig
    )
    val sessionExercise = SessionExercise(
        snapshot = dummySnapshot,
        currentConfig = dummyConfig,
        restTime = 45.seconds
    )
    val progress = ExerciseQueueProgress(
        remainingExerciseSets = 2,
        currentExerciseTotalSets = 3,
        totalExercises = 4,
        remainingExercises = 2,
        remainingSetsInWorkout = 6,
        nextSetQueueProgress = null
    )
    val state = TrainingSessionState.ExerciseState(
        sessionExercise = sessionExercise,
        timerState = TimerState.OverflowRunning(-20.seconds),
        progress = progress
    )

    TrainingPlannerTheme {
        IsometricHoldControls(
            state = state,
            onSessionIntent = {}
        )
    }
}
