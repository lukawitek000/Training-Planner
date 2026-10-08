package com.lukasz.witkowski.training.planner.training.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.shared.time.TimeFormatter
import com.lukasz.witkowski.training.planner.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.training.domain.ExerciseCategoryName
import com.lukasz.witkowski.training.planner.training.domain.ExerciseSnapshot
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.editor.TrainingExerciseOverview
import com.lukasz.witkowski.training.planner.training.editor.TrainingExercisePosition
import com.lukasz.witkowski.training.planner.training.list.TrainingPlanDateInfo
import com.lukasz.witkowski.training.planner.ui.components.CategoriesRow
import com.lukasz.witkowski.training.planner.ui.components.ConfirmButton
import com.lukasz.witkowski.training.planner.ui.components.FailureScreen
import com.lukasz.witkowski.training.planner.ui.components.LoadingScreen
import com.lukasz.witkowski.training.planner.ui.theme.Dimens
import com.lukasz.witkowski.training.planner.ui.theme.MediumGrey
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@Composable
fun TrainingPlanDetailsScreen(
    viewModel: TrainingPlanDetailsViewModel,
    modifier: Modifier = Modifier,
) {
    val trainingPlanState by viewModel.trainingPlan.collectAsState()
    TrainingPlanDetailsScreenContent(
        modifier = modifier.fillMaxSize(),
        trainingPlanState = trainingPlanState
    )
}

@Composable
private fun TrainingPlanDetailsScreenContent(
    modifier: Modifier = Modifier,
    trainingPlanState: ResultHandler<TrainingPlan>
) {
    when (trainingPlanState) {
        is ResultHandler.Loading -> {
            LoadingScreen(
                modifier = modifier,
                message = stringResource(id = R.string.loading_training_plan)
            )
        }

        is ResultHandler.Success -> {
            TrainingPlanDetails(
                modifier = modifier,
                trainingPlan = trainingPlanState.value
            )
        }

        is ResultHandler.Error -> {
            FailureScreen(
                modifier = modifier,
                message = stringResource(id = R.string.training_plan_not_found),
                title = stringResource(id = R.string.training_plan_loading_error),
            )
        }

        else -> Unit
    }
}

@Composable
private fun TrainingPlanDetails(
    modifier: Modifier = Modifier,
    trainingPlan: TrainingPlan
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(id = R.string.exercises_tab_title),
        stringResource(id = R.string.previous_sessions_tab_title)
    )

    Column(
        modifier = modifier.padding(Dimens.normal),
        verticalArrangement = Arrangement.spacedBy(Dimens.large)
    ) {
        TrainingPlanInfoSection(trainingPlan = trainingPlan)
        StartTrainingButton(onClick = { /* TODO */ })

        PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        when (selectedTabIndex) {
            0 -> ExercisesTabContent(exercises = trainingPlan.exercises)
            1 -> PreviousSessionsTabContent()
        }
    }
}

@Composable
private fun TrainingPlanInfoSection(
    trainingPlan: TrainingPlan,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.normal)
    ) {
        Text(
            text = trainingPlan.title,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = trainingPlan.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

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

        CategoriesRow(trainingPlan.categories.map { it.name })
    }
}

@Composable
private fun TrainingPlanRestTimeInfo(
    restTime: Duration,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val formattedRestTime = TimeFormatter(context).formatTime(Time(restTime.inWholeMilliseconds))
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Dimens.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Timer,
            contentDescription = null,
            tint = MediumGrey
        )
        Text(
            text = stringResource(id = R.string.rest_time_with_value, formattedRestTime),
            style = MaterialTheme.typography.bodyMedium,
            color = MediumGrey
        )
    }
}

@Composable
private fun StartTrainingButton(onClick: () -> Unit) {
    ConfirmButton(
        text = stringResource(id = R.string.start_training_session),
        onClick = onClick,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.padding(end = Dimens.small)
            )
        }
    )
}

@Composable
private fun ExercisesTabContent(
    exercises: List<TrainingExercise>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(id = R.string.exercises_count, exercises.size),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = Dimens.normal)
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(Dimens.normal)) {
            itemsIndexed(exercises) { index, exercise ->
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
    Card(modifier = modifier, shape = RoundedCornerShape(Dimens.large)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.large),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.large)
        ) {
            TrainingExercisePosition(position = index + 1)
            TrainingExerciseOverview(
                exercise = exercise,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PreviousSessionsTabContent() {
    // TODO to be added when training sessions are recorded
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            stringResource(id = R.string.no_previous_sessions),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Preview
@Composable
private fun TrainingPlanDetailsScreenContentLoadingPreview() {
    TrainingPlannerTheme {
        TrainingPlanDetailsScreenContent(
            trainingPlanState = ResultHandler.Loading,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
private fun TrainingPlanDetailsScreenContentFailurePreview() {
    TrainingPlannerTheme {
        TrainingPlanDetailsScreenContent(
            trainingPlanState = ResultHandler.Error("Some error"),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
private fun TrainingPlanDetailsScreenContentPreview() {
    TrainingPlannerTheme {
        Surface() {
            TrainingPlanDetailsScreenContent(
                trainingPlanState = ResultHandler.Success(
                    value = previewTrainingPlan
                ),
                modifier = Modifier.fillMaxSize()
            )
        }
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
        ),
        TrainingExercise(
            id = TrainingExerciseId.create(),
            exercise = ExerciseSnapshot(
                id = ExerciseId.create(),
                name = "Pull ups",
                description = "Back exercise",
                categories = setOf(ExerciseCategoryName("Back"))
            ),
            repetitions = 8,
            sets = 3,
            restTime = 90.seconds
        )
    ),
    restTime = 60.seconds,
    lastModification = Instant.parse("2026-10-01T12:00:00Z"),
    lastSession = Instant.parse("2026-10-05T12:00:00Z")
)

