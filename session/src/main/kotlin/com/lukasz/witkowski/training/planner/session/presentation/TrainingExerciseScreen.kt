package com.lukasz.witkowski.training.planner.session.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.lukasz.witkowski.training.planner.exercise.presentation.PREVIEW_PUSH_UP_IMAGES
import com.lukasz.witkowski.training.planner.image.presentation.ImageGallery
import com.lukasz.witkowski.training.planner.session.R
import com.lukasz.witkowski.training.planner.session.presentation.components.AppIntStepper
import com.lukasz.witkowski.training.planner.session.presentation.components.AppStepper
import com.lukasz.witkowski.training.planner.shared.theme.Dimens
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.button.AppPrimaryButton
import com.lukasz.witkowski.training.planner.shared.ui.button.AppSecondaryButton
import com.lukasz.witkowski.training.planner.shared.ui.button.AppTertiaryButton
import com.lukasz.witkowski.training.planner.shared.ui.card.AppCard
import com.lukasz.witkowski.training.planner.shared.ui.chips.AppCategoryPill
import com.lukasz.witkowski.training.planner.training.domain.ExerciseCategoryName
import com.lukasz.witkowski.training.planner.training.domain.ExerciseSnapshot
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Active workout session screen displaying current exercise, offline image illustration,
 * set progression, reps/hold-time/weight steppers, and set completion controls.
 */
@Composable
fun TrainingExerciseScreen(
    state: TrainingSessionState.ExerciseState,
    onRepsChanged: (Int) -> Unit,
    onWeightChanged: (Double) -> Unit,
    onHoldDurationChanged: (Duration) -> Unit,
    onToggleHoldTimer: () -> Unit,
    onResetHoldTimer: () -> Unit,
    onCompleteSet: () -> Unit,
    onSkipSet: () -> Unit,
    onRemoveSet: () -> Unit,
    onAddExtraSet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.margin, vertical = Dimens.spaceMd),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceMd)
    ) {
        ExerciseHeader(
            exerciseName = state.currentExercise.exercise.name,
            categories = state.currentExercise.exercise.categories.map { it.name },
            exerciseIndex = state.exerciseIndex,
            totalExercises = state.totalExercises,
        )

        if (state.exerciseImages.isNotEmpty()) {
            ImageGallery(
                images = state.exerciseImages,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.heroImageHeightCompact),
            )
        }

        SetsProgressStrip(
            currentSet = state.currentSet,
            totalSets = state.totalSets
        )

        ActiveSetDetails(
            state = state,
            onToggleHoldTimer = onToggleHoldTimer,
            onResetHoldTimer = onResetHoldTimer,
            onHoldDurationChanged = onHoldDurationChanged,
            onRepsChanged = onRepsChanged,
            onWeightChanged = onWeightChanged
        )

        // Secondary Actions (Add extra set, skip exercise)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppTertiaryButton(
                text = stringResource(R.string.session_add_extra_set),
                leadingIcon = Icons.Default.Add,
                onClick = onAddExtraSet,
                modifier = Modifier.weight(1f)
            )

            AppTertiaryButton(
                text = stringResource(R.string.session_remove_set),
                leadingIcon = Icons.Default.Remove,
                onClick = onRemoveSet,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(Dimens.spaceMd))

        // Main CTA Area
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceMd)
        ) {
            AppPrimaryButton(
                text = stringResource(R.string.session_complete_set),
                leadingIcon = Icons.Default.Check,
                onClick = onCompleteSet
            )

            AppSecondaryButton(
                text = stringResource(R.string.session_skip_set),
                leadingIcon = Icons.Default.SkipNext,
                onClick = onSkipSet
            )
        }
    }
}

@Composable
private fun ActiveSetDetails(
    state: TrainingSessionState.ExerciseState,
    onToggleHoldTimer: () -> Unit,
    onResetHoldTimer: () -> Unit,
    onHoldDurationChanged: (Duration) -> Unit,
    onRepsChanged: (Int) -> Unit,
    onWeightChanged: (Double) -> Unit
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.session_active_set_format, state.currentSet),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                val targetMetric =
                    if (state.isIsometric) {
                        stringResource(
                            R.string.session_target_hold_format,
                            state.targetTime.inWholeSeconds
                        )
                    } else {
                        stringResource(
                            R.string.session_target_reps_format,
                            state.targetReps
                        )
                    }
                val weight = state.targetWeightInKg
                val weightSuffix =
                    if (weight != null && weight > 0.0) {
                        stringResource(
                            R.string.session_target_weight_suffix,
                            weight.toString()
                        )
                    } else {
                        ""
                    }
                val summary = stringResource(
                    R.string.session_target_summary_format,
                    targetMetric
                )
                Text(
                    text = summary + weightSuffix,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (state.isIsometric) {
                IsometricHoldControls(
                    countdownRemaining = state.remainingHoldTime.inWholeSeconds.toInt(),
                    isHoldTimerRunning = state.isHoldTimerRunning,
                    onToggleTimer = onToggleHoldTimer,
                    onResetTimer = onResetHoldTimer,
                    holdSeconds = state.selectedHoldDuration.inWholeSeconds.toInt(),
                    onHoldSecondsChange = { onHoldDurationChanged(it.seconds) }
                )
            } else {
                AppIntStepper(
                    value = state.selectedReps,
                    onValueChange = onRepsChanged,
                    label = stringResource(R.string.session_reps_label),
                    minValue = 0
                )
            }

            AppStepper(
                value = state.selectedWeightInKg ?: 0.0,
                onValueChange = onWeightChanged,
                step = 2.5,
                label = stringResource(R.string.session_kg_label),
                minValue = 0.0
            )
        }
    }
}

@Composable
private fun IsometricHoldControls(
    countdownRemaining: Int,
    isHoldTimerRunning: Boolean,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    holdSeconds: Int,
    onHoldSecondsChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(horizontal = Dimens.spaceMd, vertical = Dimens.spaceSm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimens.iconSize24)
                )
                Text(
                    text = stringResource(
                        R.string.session_timer_remaining_format,
                        countdownRemaining / 60,
                        countdownRemaining % 60
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onToggleTimer) {
                val icon =
                    if (isHoldTimerRunning) {
                        Icons.Default.Pause
                    } else {
                        Icons.Default.PlayArrow
                    }
                val descRes =
                    if (isHoldTimerRunning) {
                        R.string.session_pause_hold_timer
                    } else {
                        R.string.session_start_hold_timer
                    }
                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(descRes),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        AppIntStepper(
            value = holdSeconds,
            onValueChange = onHoldSecondsChange,
            step = 5,
            label = stringResource(R.string.session_sec_hold_label),
            minValue = 0
        )
    }
}

@Composable
private fun ExerciseHeader(
    exerciseName: String,
    categories: List<String>,
    exerciseIndex: Int,
    totalExercises: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceXs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val defaultCategory = stringResource(R.string.session_category_general)
            val categoryLabel = categories.firstOrNull() ?: defaultCategory
            AppCategoryPill(text = categoryLabel)

            Text(
                text = stringResource(
                    R.string.session_exercise_progress_format,
                    exerciseIndex,
                    totalExercises
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = exerciseName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
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

@AppPreview
@Composable
private fun TrainingExerciseScreenPreview() {
    val dummyExercise = TrainingExercise(
        id = TrainingExerciseId.create(),
        exercise = ExerciseSnapshot(
            id = com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId.create(),
            name = "Barbell Bench Press",
            description = "Lie on bench, press bar upwards",
            categories = setOf(ExerciseCategoryName("Chest")),
            imageIds = PREVIEW_PUSH_UP_IMAGES.map { it.imageId }
        ),
        repetitions = 10,
        sets = 4,
        restTime = 90.seconds
    )
    val state = TrainingSessionState.ExerciseState(
        currentExercise = dummyExercise,
        exerciseImages = PREVIEW_PUSH_UP_IMAGES,
        currentSet = 2,
        totalSets = 4,
        targetReps = 10,
        targetWeightInKg = 85.0,
        completedSets = emptyList(),
        exerciseIndex = 1,
        totalExercises = 4,
        setsLeftInWorkout = 10
    )

    TrainingPlannerTheme {
        TrainingExerciseScreen(
            state = state,
            onRepsChanged = {},
            onWeightChanged = {},
            onHoldDurationChanged = {},
            onToggleHoldTimer = {},
            onResetHoldTimer = {},
            onCompleteSet = {},
            onSkipSet = {},
            onRemoveSet = {},
            onAddExtraSet = {}
        )
    }
}

@AppPreview
@Composable
private fun TrainingExerciseScreenHoldTimePreview() {
    val dummyExercise = TrainingExercise(
        id = TrainingExerciseId.create(),
        exercise = ExerciseSnapshot(
            id = com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId.create(),
            name = "Plank",
            description = "Hold a straight body position resting on forearms and toes",
            categories = setOf(ExerciseCategoryName("Core")),
            imageIds = emptyList()
        ),
        repetitions = 0,
        sets = 3,
        time = 60.seconds,
        restTime = 45.seconds
    )
    val state = TrainingSessionState.ExerciseState(
        currentExercise = dummyExercise,
        exerciseImages = emptyList(),
        currentSet = 1,
        totalSets = 3,
        targetReps = 0,
        targetWeightInKg = 0.0,
        completedSets = emptyList(),
        exerciseIndex = 2,
        totalExercises = 4,
        setsLeftInWorkout = 6,
        targetTime = 60.seconds,
        remainingHoldTime = 42.seconds,
        isHoldTimerRunning = true
    )

    TrainingPlannerTheme {
        TrainingExerciseScreen(
            state = state,
            onRepsChanged = {},
            onWeightChanged = {},
            onHoldDurationChanged = {},
            onToggleHoldTimer = {},
            onResetHoldTimer = {},
            onCompleteSet = {},
            onSkipSet = {},
            onRemoveSet = {},
            onAddExtraSet = {}
        )
    }
}
