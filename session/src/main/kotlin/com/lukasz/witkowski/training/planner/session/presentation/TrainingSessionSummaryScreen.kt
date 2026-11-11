package com.lukasz.witkowski.training.planner.session.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.session.R
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseAttemptStatisticsId
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.ExerciseStatisticsId
import com.lukasz.witkowski.training.planner.session.domain.models.SessionRpe
import com.lukasz.witkowski.training.planner.session.domain.models.SetEffort
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.presentation.components.AppRpeScaleGrid
import com.lukasz.witkowski.training.planner.session.presentation.models.TrainingSessionUiState
import com.lukasz.witkowski.training.planner.shared.theme.Dimens
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.time.formatToString
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.bento.AppBentoTile
import com.lukasz.witkowski.training.planner.shared.ui.button.AppPrimaryButton
import com.lukasz.witkowski.training.planner.shared.ui.inputField.AppLabeledTextField
import com.lukasz.witkowski.training.planner.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.training.domain.ExerciseCategoryName
import com.lukasz.witkowski.training.planner.training.domain.ExerciseSnapshot
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import java.util.Date
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Workout session summary screen displaying performance metrics, RPE exertion selector,
 * session notes, exercise attempts breakdown, and save action.
 */
@Composable
fun TrainingSessionSummaryScreen(
    uiState: TrainingSessionUiState.Summary,
    onSessionIntent: (SessionIntent) -> Unit,
    onFinishWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val state = uiState.domainState
    val statistics = state.statistics

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = Dimens.margin, vertical = Dimens.spaceMd),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceLg)
    ) {
        CelebrationBanner(
            trainingPlanResult = uiState.trainingPlan,
            date = statistics.date
        )

        BentoMetricsGrid(statistics = statistics)

        AppRpeScaleGrid(
            selectedRpe = state.selectedOverallRpe,
            onRpeSelected = { onSessionIntent(SessionIntent.SessionFeedbackSelected(it)) }
        )

        AppLabeledTextField(
            label = stringResource(R.string.session_summary_notes_label),
            value = state.notes,
            onValueChange = { onSessionIntent(SessionIntent.SessionNotesChanged(it)) },
            placeholder = stringResource(R.string.session_summary_notes_placeholder),
            singleLine = false,
            minLines = 3
        )

        ExercisesBreakdownSection(
            statistics = statistics,
            trainingPlanResult = uiState.trainingPlan
        )

        AppPrimaryButton(
            text = stringResource(R.string.session_finish),
            leadingIcon = Icons.Default.Celebration,
            onClick = onFinishWorkout,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CelebrationBanner(
    trainingPlanResult: ResultHandler<TrainingPlan>,
    date: Date,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.iconContainerSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = stringResource(R.string.session_workout_complete),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(Dimens.iconSize30)
            )
        }

        Text(
            text = stringResource(R.string.session_workout_complete),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        val text = if (trainingPlanResult is ResultHandler.Success) {
            "${trainingPlanResult.value.title} • ${date.formatToString()}"
        } else {
            date.formatToString()
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BentoMetricsGrid(statistics: TrainingStatistics, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
        ) {
            AppBentoTile(
                label = stringResource(R.string.session_bento_duration_label),
                value = statistics.totalTime.toString(),
                subtitle = stringResource(R.string.session_bento_duration_sub),
                modifier = Modifier.weight(1f)
            )
            AppBentoTile(
                label = stringResource(R.string.session_bento_volume_label),
                value = if (statistics.totalVolumeKg > 0) {
                    stringResource(R.string.session_kg_suffix, statistics.totalVolumeKg.toInt())
                } else {
                    stringResource(R.string.session_bodyweight)
                },
                subtitle = stringResource(R.string.session_bento_volume_sub),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
        ) {
            AppBentoTile(
                label = stringResource(R.string.session_bento_sets_label),
                value = "${statistics.completedSetsCount} / ${statistics.totalSetsCount}",
                subtitle = stringResource(R.string.session_bento_sets_sub),
                modifier = Modifier.weight(1f)
            )
            AppBentoTile(
                label = stringResource(R.string.session_bento_reps_label),
                value = stringResource(R.string.session_reps_suffix, statistics.totalReps),
                subtitle = stringResource(R.string.session_bento_reps_sub),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ExercisesBreakdownSection(
    statistics: TrainingStatistics,
    trainingPlanResult: ResultHandler<TrainingPlan>,
    modifier: Modifier = Modifier
) {
    // TODO implement when working on statistics
}

@AppPreview
@Composable
private fun TrainingSessionSummaryScreenPreview() {
    val stats = TrainingStatistics(
        id = TrainingStatisticsId.create(),
        trainingPlanId = TrainingPlanId.create(),
        totalTime = 2700000L.milliseconds,
        date = Date(),
        exercisesStatistics = listOf(
            ExerciseStatistics(
                id = ExerciseStatisticsId.create(),
                trainingExerciseId = TrainingExerciseId.create(),
                attemptsStatistics = listOf(
                    ExerciseAttemptStatistics(
                        id = ExerciseAttemptStatisticsId.create(),
                        trainingExerciseId = TrainingExerciseId.create(),
                        time = 12_000L.milliseconds,
                        set = 1,
                        completed = true,
                        actualReps = 10,
                        actualTime = 30.seconds,
                        effort = SetEffort.HARD,
                        restTime = 30.seconds
                    ),
                    ExerciseAttemptStatistics(
                        id = ExerciseAttemptStatisticsId.create(),
                        trainingExerciseId = TrainingExerciseId.create(),
                        time = 12_000L.milliseconds,
                        set = 2,
                        completed = false,
                        actualReps = 10,
                        actualTime = 30.seconds,
                        effort = SetEffort.HARD,
                        restTime = 30.seconds
                    )
                )
            ),
            ExerciseStatistics(
                id = ExerciseStatisticsId.create(),
                trainingExerciseId = TrainingExerciseId.create(),
                attemptsStatistics = listOf(
                    ExerciseAttemptStatistics(
                        id = ExerciseAttemptStatisticsId.create(),
                        trainingExerciseId = TrainingExerciseId.create(),
                        time = 12_000L.milliseconds,
                        set = 1,
                        completed = true,
                        actualReps = 10,
                        actualTime = 30.seconds,
                        effort = SetEffort.HARD,
                        restTime = 30.seconds
                    ),
                    ExerciseAttemptStatistics(
                        id = ExerciseAttemptStatisticsId.create(),
                        trainingExerciseId = TrainingExerciseId.create(),
                        time = 12_000L.milliseconds,
                        set = 2,
                        completed = false,
                        actualReps = 10,
                        actualTime = 30.seconds,
                        effort = SetEffort.HARD,
                        restTime = 30.seconds
                    )
                )
            )
        )
    )

    TrainingPlannerTheme {
        TrainingSessionSummaryScreen(
            uiState = TrainingSessionUiState.Summary(
                TrainingSessionState.SummaryState(
                    statistics = stats,
                    selectedOverallRpe = SessionRpe.VERY_HARD,
                    notes = "Great workout!"
                ),
                trainingPlan = ResultHandler.Success(PREVIEW_TRAINING_PLAN)
            ),
            onSessionIntent = {},
            onFinishWorkout = {}
        )
    }
}

private val PREVIEW_TRAINING_PLAN = TrainingPlan(
    id = TrainingPlanId.create(),
    title = "Full Body Workout",
    description = "A comprehensive full body routine covering chest, legs, back, and core.",
    exercises = listOf(
        TrainingExercise(
            id = TrainingExerciseId.create(),
            exercise = ExerciseSnapshot(
                id = ExerciseId.create(),
                name = "Push ups",
                description = "Chest exercise",
                categories = setOf(ExerciseCategoryName("Chest"), ExerciseCategoryName("Arms"))
            ),
            repetitions = 10,
            sets = 3,
            restTime = 60.seconds
        ),
        TrainingExercise(
            id = TrainingExerciseId.create(),
            exercise = ExerciseSnapshot(
                id = ExerciseId.create(),
                name = "Squats",
                description = "Leg exercise",
                categories = setOf(ExerciseCategoryName("Legs"))
            ),
            repetitions = 12,
            sets = 4,
            restTime = 60.seconds
        )
    ),
    restTime = 60.seconds,
    lastModification = Instant.parse("2026-10-01T12:00:00Z"),
    lastSession = Instant.parse("2026-10-05T12:00:00Z")
)
