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
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.lukasz.witkowski.training.planner.session.presentation.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.presentation.components.AppIntStepper
import com.lukasz.witkowski.training.planner.session.presentation.components.AppStepper
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Active workout session screen displaying current exercise, offline image illustration,
 * set progression, reps/hold-time/weight steppers, and set completion controls.
 */
@Composable
fun TrainingExerciseScreen(
    state: TrainingSessionState.ExerciseState,
    onCompleteSet: (actualReps: Int, actualWeight: Double?, actualTime: Duration?) -> Unit,
    onSkipSet: () -> Unit,
    onSkipExercise: () -> Unit,
    onAddExtraSet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var reps by remember(state.currentExercise.id, state.currentSet) {
        mutableIntStateOf(state.targetReps)
    }
    var weight by remember(state.currentExercise.id, state.currentSet) {
        mutableDoubleStateOf(state.targetWeightInKg ?: 0.0)
    }
    var holdSeconds by remember(state.currentExercise.id, state.currentSet) {
        mutableIntStateOf(state.targetTime.inWholeSeconds.toInt())
    }
    var isHoldTimerRunning by remember(state.currentExercise.id, state.currentSet) {
        mutableStateOf(false)
    }
    var countdownRemaining by remember(state.currentExercise.id, state.currentSet) {
        mutableIntStateOf(state.targetTime.inWholeSeconds.toInt())
    }

    LaunchedEffect(isHoldTimerRunning, state.currentExercise.id, state.currentSet) {
        if (isHoldTimerRunning) {
            while (isActive && countdownRemaining > 0) {
                delay(1000L)
                countdownRemaining--
                holdSeconds =
                    (state.targetTime.inWholeSeconds.toInt() - countdownRemaining).coerceAtLeast(0)
            }
            if (countdownRemaining == 0) {
                isHoldTimerRunning = false
                holdSeconds = state.targetTime.inWholeSeconds.toInt()
                // TODO: Play audio chime and trigger haptic vibration when isometric hold countdown completes
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = Dimens2.margin, vertical = Dimens2.spaceMd),
        verticalArrangement = Arrangement.spacedBy(Dimens2.spaceMd)
    ) {
        // Exercise Header
        ExerciseHeader(
            exerciseName = state.currentExercise.exercise.name,
            categories = state.currentExercise.exercise.categories.map { it.name },
            exerciseIndex = state.exerciseIndex,
            totalExercises = state.totalExercises,
            followingExerciseName = state.followingExerciseName
        )

        // Exercise Illustration Hero Card
        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens2.heroImageHeightCompact),
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            AppExerciseImage(
                imageId = state.currentExercise.exercise.imageId,
                contentDescription = state.currentExercise.exercise.name,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Sets Progress Strip
        SetsProgressStrip(
            currentSet = state.currentSet,
            totalSets = state.totalSets
        )

        // Active Set Card
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens2.spaceMd),
                verticalArrangement = Arrangement.spacedBy(Dimens2.spaceMd)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE SET ${state.currentSet}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val targetSummary = if (state.isIsometric) {
                        "${state.targetTime.inWholeSeconds}s hold"
                    } else {
                        "${state.targetReps} reps"
                    }
                    Text(
                        text = "Target: $targetSummary" +
                            if (state.targetWeightInKg != null &&
                                state.targetWeightInKg > 0.0
                            ) {
                                " • ${state.targetWeightInKg} kg"
                            } else {
                                ""
                            },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (state.isIsometric) {
                    // Isometric Hold Timer & Stepper Controls
                    IsometricHoldControls(
                        countdownRemaining = countdownRemaining,
                        isHoldTimerRunning = isHoldTimerRunning,
                        onToggleTimer = { isHoldTimerRunning = !isHoldTimerRunning },
                        onResetTimer = {
                            isHoldTimerRunning = false
                            countdownRemaining = state.targetTime.inWholeSeconds.toInt()
                            holdSeconds = state.targetTime.inWholeSeconds.toInt()
                        },
                        holdSeconds = holdSeconds,
                        onHoldSecondsChange = { holdSeconds = it }
                    )
                } else {
                    // Reps Stepper
                    AppIntStepper(
                        value = reps,
                        onValueChange = { reps = it },
                        label = "reps",
                        minValue = 0
                    )
                }

                // Weight Stepper
                AppStepper(
                    value = weight,
                    onValueChange = { weight = it },
                    step = 2.5,
                    label = "kg",
                    minValue = 0.0
                )
            }
        }

        // Secondary Actions (Add extra set, skip exercise)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceSm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppTertiaryButton(
                text = "Add Extra Set",
                leadingIcon = Icons.Default.Add,
                onClick = onAddExtraSet,
                modifier = Modifier.weight(1f)
            )

            AppTertiaryButton(
                text = "Skip Exercise",
                leadingIcon = Icons.Default.FastForward,
                onClick = onSkipExercise,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(Dimens2.spaceMd))

        // Main CTA Area
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
        ) {
            AppPrimaryButton(
                text = "Complete Set",
                leadingIcon = Icons.Default.Check,
                onClick = {
                    val actualReps = if (state.isIsometric) 1 else reps
                    val actualWeight = if (weight > 0.0) weight else null
                    val actualTime = if (state.isIsometric) holdSeconds.seconds else null
                    onCompleteSet(actualReps, actualWeight, actualTime)
                }
            )

            AppSecondaryButton(
                text = "Skip Set",
                leadingIcon = Icons.Default.SkipNext,
                onClick = onSkipSet
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
        verticalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
    ) {
        // Live Hold Countdown Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(horizontal = Dimens2.spaceMd, vertical = Dimens2.spaceSm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceSm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimens2.iconSize24)
                )
                Text(
                    text = "%02d:%02d remaining".format(
                        countdownRemaining / 60,
                        countdownRemaining % 60
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceXs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleTimer) {
                    val icon =
                        if (isHoldTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow
                    val desc = if (isHoldTimerRunning) "Pause Hold Timer" else "Start Hold Timer"
                    Icon(
                        imageVector = icon,
                        contentDescription = desc,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onResetTimer) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Hold Timer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Hold Seconds Stepper
        AppIntStepper(
            value = holdSeconds,
            onValueChange = onHoldSecondsChange,
            step = 5,
            label = "sec hold",
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
    followingExerciseName: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens2.spaceXs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val categoryLabel = categories.firstOrNull() ?: "General"
            AppCategoryPill(text = categoryLabel)

            Text(
                text = "EXERCISE $exerciseIndex OF $totalExercises",
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

        if (followingExerciseName != null) {
            Text(
                text = "Up next: $followingExerciseName",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SetsProgressStrip(currentSet: Int, totalSets: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceSm),
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
                    .size(Dimens2.setChipSize)
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .border(
                        width = if (isCurrent) Dimens2.mediumBorder else Dimens2.thinBorder,
                        color = borderColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed set $setIndex",
                        tint = textColor,
                        modifier = Modifier.size(Dimens2.iconSize16)
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
            imageId = null
        ),
        repetitions = 10,
        sets = 4,
        restTime = 90.seconds
    )
    val state = TrainingSessionState.ExerciseState(
        currentExercise = dummyExercise,
        currentSet = 2,
        totalSets = 4,
        targetReps = 10,
        targetWeightInKg = 85.0,
        completedSets = emptyList(),
        exerciseIndex = 1,
        totalExercises = 4,
        setsLeftInWorkout = 10,
        followingExerciseName = "Incline Dumbbell Press (3 sets)"
    )

    TrainingPlannerTheme2 {
        TrainingExerciseScreen(
            state = state,
            onCompleteSet = { _, _, _ -> },
            onSkipSet = {},
            onSkipExercise = {},
            onAddExtraSet = {}
        )
    }
}
