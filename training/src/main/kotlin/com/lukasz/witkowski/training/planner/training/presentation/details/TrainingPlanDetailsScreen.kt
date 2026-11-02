package com.lukasz.witkowski.training.planner.training.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.shared.time.TimeFormatter
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.button.AppPrimaryButton
import com.lukasz.witkowski.training.planner.shared.ui.card.AppCard
import com.lukasz.witkowski.training.planner.shared.ui.chips.AppCategoryPill
import com.lukasz.witkowski.training.planner.shared.ui.chips.AppCategoryPillStyle
import com.lukasz.witkowski.training.planner.shared.ui.feedback.AppEmptyState
import com.lukasz.witkowski.training.planner.shared.ui.feedback.AppErrorState
import com.lukasz.witkowski.training.planner.shared.ui.loading.AppLoadingState
import com.lukasz.witkowski.training.planner.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.training.R
import com.lukasz.witkowski.training.planner.training.domain.ExerciseCategoryName
import com.lukasz.witkowski.training.planner.training.domain.ExerciseSnapshot
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.presentation.list.TrainingPlanDateInfo
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@Composable
fun TrainingPlanDetailsScreen(
    viewModel: TrainingPlanDetailsViewModel,
    modifier: Modifier = Modifier,
    onStartTrainingSessionClicked: (TrainingPlanId) -> Unit = {}
) {
    val trainingPlanState by viewModel.trainingPlan.collectAsState()
    TrainingPlanDetailsScreenContent(
        modifier = modifier.fillMaxSize(),
        trainingPlanState = trainingPlanState,
        onStartTrainingSessionClicked = onStartTrainingSessionClicked
    )
}

@Composable
fun TrainingPlanDetailsScreenContent(
    trainingPlanState: ResultHandler<TrainingPlan>,
    modifier: Modifier = Modifier,
    onStartTrainingSessionClicked: (TrainingPlanId) -> Unit = {}
) {
    when (trainingPlanState) {
        is ResultHandler.Loading -> {
            AppLoadingState(
                title = stringResource(id = R.string.loading_training_plan),
                modifier = modifier
            )
        }

        is ResultHandler.Success -> {
            TrainingPlanDetails(
                modifier = modifier,
                trainingPlan = trainingPlanState.value,
                onStartTrainingSessionClicked = onStartTrainingSessionClicked
            )
        }

        is ResultHandler.Error -> {
            AppErrorState(
                title = stringResource(id = R.string.training_plan_loading_error),
                subtitle = stringResource(id = R.string.training_plan_not_found),
                modifier = modifier
            )
        }

        else -> Unit
    }
}

@Composable
private fun TrainingPlanDetails(
    trainingPlan: TrainingPlan,
    modifier: Modifier = Modifier,
    onStartTrainingSessionClicked: (TrainingPlanId) -> Unit = {}
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(id = R.string.exercises_tab_title),
        stringResource(id = R.string.previous_sessions_tab_title)
    )

    Column(
        modifier = modifier.padding(Dimens2.spaceMd),
        verticalArrangement = Arrangement.spacedBy(Dimens2.spaceMd)
    ) {
        TrainingPlanInfoSection(trainingPlan = trainingPlan)
        AppPrimaryButton(
            text = stringResource(id = R.string.start_training_session),
            onClick = { onStartTrainingSessionClicked(trainingPlan.id) },
            leadingIcon = Icons.Default.PlayArrow,
            modifier = Modifier.fillMaxWidth()
        )

        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                )
            }
        }
        when (selectedTabIndex) {
            0 -> ExercisesTabContent(
                exercises = trainingPlan.exercises,
                modifier = Modifier.weight(1f)
            )
            1 -> PreviousSessionsTabContent(
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TrainingPlanInfoSection(trainingPlan: TrainingPlan, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
    ) {
        Text(
            text = trainingPlan.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (trainingPlan.description.isNotEmpty()) {
            Text(
                text = trainingPlan.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TrainingPlanRestTimeInfo(restTime = trainingPlan.restTime)

        TrainingPlanDateInfo(
            instant = trainingPlan.lastModification,
            icon = Icons.Default.CalendarMonth,
            stringRes = R.string.last_modified
        )
        trainingPlan.lastSession?.let {
            TrainingPlanDateInfo(
                instant = it,
                icon = Icons.Default.PlayArrow,
                stringRes = R.string.last_session
            )
        }

        if (trainingPlan.categories.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceXs),
                verticalArrangement = Arrangement.spacedBy(Dimens2.spaceXs)
            ) {
                trainingPlan.categories.forEach { category ->
                    AppCategoryPill(
                        text = category.name,
                        style = AppCategoryPillStyle.Tonal
                    )
                }
            }
        }
    }
}

@Composable
private fun TrainingPlanRestTimeInfo(restTime: Duration, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val formattedRestTime = TimeFormatter(context).formatTime(Time(restTime.inWholeMilliseconds))
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceXs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Timer,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(id = R.string.rest_time_with_value, formattedRestTime),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ExercisesTabContent(exercises: List<TrainingExercise>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(id = R.string.exercises_count, exercises.size),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = Dimens2.spaceSm)
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)) {
            itemsIndexed(exercises, key = { _, it -> it.id.toString() }) { index, exercise ->
                ExerciseListItem(index = index, exercise = exercise)
            }
        }
    }
}

@Composable
private fun ExerciseListItem(
    index: Int,
    exercise: TrainingExercise,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens2.spaceMd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceMd)
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens2.spaceXl)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens2.spaceXs)
            ) {
                Text(
                    text = exercise.exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text =
                    "${stringResource(
                        R.string.sets_with_value,
                        exercise.sets
                    )} | ${stringResource(R.string.reps_with_value, exercise.repetitions)}" +
                        if (exercise.weightInKg !=
                            null
                        ) {
                            " | ${exercise.weightInKg} ${stringResource(R.string.kg)}"
                        } else {
                            ""
                        },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PreviousSessionsTabContent(modifier: Modifier = Modifier) {
    AppEmptyState(
        title = stringResource(id = R.string.no_previous_sessions),
        modifier = modifier
    )
}

@AppPreview
@Composable
private fun TrainingPlanDetailsScreenContentLoadingPreview() {
    TrainingPlannerTheme2 {
        TrainingPlanDetailsScreenContent(
            trainingPlanState = ResultHandler.Loading,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@AppPreview
@Composable
private fun TrainingPlanDetailsScreenContentFailurePreview() {
    TrainingPlannerTheme2 {
        TrainingPlanDetailsScreenContent(
            trainingPlanState = ResultHandler.Error("Some error"),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@AppPreview
@Composable
private fun TrainingPlanDetailsScreenContentPreview() {
    TrainingPlannerTheme2 {
        TrainingPlanDetailsScreenContent(
            trainingPlanState = ResultHandler.Success(value = previewTrainingPlan),
            modifier = Modifier.fillMaxSize()
        )
    }
}

private val previewTrainingPlan = TrainingPlan(
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
