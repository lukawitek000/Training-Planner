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
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.lukasz.witkowski.training.planner.session.presentation.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.presentation.components.AppFeedbackChips
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.button.AppPrimaryButton
import com.lukasz.witkowski.training.planner.shared.ui.button.AppSecondaryButton
import com.lukasz.witkowski.training.planner.shared.ui.card.AppCard
import com.lukasz.witkowski.training.planner.shared.ui.timer.AppCircularTimer
import com.lukasz.witkowski.training.planner.training.domain.ExerciseCategoryName
import com.lukasz.witkowski.training.planner.training.domain.ExerciseSnapshot
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import kotlin.time.Duration.Companion.seconds

/**
 * Rest interval screen displaying circular countdown ring, set difficulty feedback chips,
 * and up-next exercise preview.
 */
@Composable
fun RestTimeScreen(
    state: TrainingSessionState.RestTimeState,
    timeLeft: Time,
    totalTime: Time,
    onStartNextSet: () -> Unit,
    onSkipRest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var selectedFeedback by remember { mutableStateOf<String?>(null) }

    val progress = if (totalTime.timeInMillis > 0) {
        timeLeft.timeInMillis.toFloat() / totalTime.timeInMillis.toFloat()
    } else {
        0f
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = Dimens2.margin, vertical = Dimens2.spaceMd),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens2.spaceMd)
    ) {
        // Rest Header
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = Dimens2.spaceMd, vertical = Dimens2.spaceXs)
        ) {
            Text(
                text = "REST INTERVAL",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        // Circular Timer Ring
        // TODO: Play 3-2-1 audio beeps and trigger vibration haptic pulses as rest interval finishes
        AppCircularTimer(
            timeFormatted = formatRemainingTime(timeLeft),
            progress = progress,
            label = "Remaining"
        )

        // Preceding Set Feedback Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens2.spaceXs)
        ) {
            Text(
                text = "HOW DID THAT SET FEEL?",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            AppFeedbackChips(
                options = listOf("Easy", "Good", "Hard", "Limit"),
                selectedOption = selectedFeedback,
                onOptionSelected = { selectedFeedback = it },
                labelProvider = { it }
            )
        }

        // Up-Next Preview Card
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens2.spaceMd),
                horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceMd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppExerciseImage(
                    imageId = state.nextExercise.exercise.imageId,
                    contentDescription = state.nextExercise.exercise.name,
                    modifier = Modifier.size(Dimens2.listItemThumbnailSize)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Dimens2.spaceXs)
                ) {
                    Text(
                        text = "UP NEXT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = state.nextExercise.exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Set ${state.nextSet} of ${state.totalSetsForNextExercise}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val targetSummary = if (state.nextExercise.isIsometric) {
                            "${state.nextExercise.time.inWholeSeconds}s hold"
                        } else {
                            "${state.nextExercise.repetitions} reps"
                        }
                        val weight = state.nextExercise.weightInKg
                        Text(
                            text = targetSummary +
                                if (weight != null && weight > 0.0) " • $weight kg" else "",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (state.followingExerciseName != null) {
                        Text(
                            text = "Following: ${state.followingExerciseName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens2.spaceMd))

        // CTA Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
        ) {
            AppPrimaryButton(
                text = "Start Next Set Now",
                leadingIcon = Icons.Default.PlayArrow,
                onClick = onStartNextSet
            )

            AppSecondaryButton(
                text = "Skip Rest",
                leadingIcon = Icons.Default.SkipNext,
                onClick = onSkipRest
            )
        }
    }
}

private fun formatRemainingTime(time: Time): String {
    val totalSeconds = (time.timeInMillis / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@AppPreview
@Composable
private fun RestTimeScreenPreview() {
    val dummyExercise = TrainingExercise(
        id = TrainingExerciseId.create(),
        exercise = ExerciseSnapshot(
            id = com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId.create(),
            name = "Incline Dumbbell Press",
            description = "Press dumbbells at incline",
            categories = setOf(ExerciseCategoryName("Chest")),
            imageId = null
        ),
        repetitions = 12,
        sets = 3,
        restTime = 60.seconds
    )
    val state = TrainingSessionState.RestTimeState(
        nextExercise = dummyExercise,
        restTime = Time(60000L),
        nextSet = 2,
        totalSetsForNextExercise = 3,
        lastCompletedAttempt = null,
        followingExerciseName = "Cable Fly (3 sets)"
    )

    TrainingPlannerTheme2 {
        RestTimeScreen(
            state = state,
            timeLeft = Time(42000L),
            totalTime = Time(60000L),
            onStartNextSet = {},
            onSkipRest = {}
        )
    }
}
