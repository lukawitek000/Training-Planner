package com.lukasz.witkowski.training.planner.training.list

import android.text.format.DateUtils
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.training.domain.ExerciseCategoryName
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanOverview
import com.lukasz.witkowski.training.planner.ui.components.CategoriesRow
import com.lukasz.witkowski.training.planner.ui.components.FailureScreen
import com.lukasz.witkowski.training.planner.ui.components.FilteringState
import com.lukasz.witkowski.training.planner.ui.components.ListCardItem
import com.lukasz.witkowski.training.planner.ui.components.LoadingScreen
import com.lukasz.witkowski.training.planner.ui.components.NoDataMessage
import com.lukasz.witkowski.training.planner.ui.components.SearchHeader
import com.lukasz.witkowski.training.planner.ui.theme.Dimens
import com.lukasz.witkowski.training.planner.ui.theme.MediumGrey
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme
import kotlin.time.Instant

@Composable
fun TrainingsScreen(
    viewModel: TrainingsListViewModel,
    onTrainingPlanClicked: (TrainingPlanId) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    TrainingsScreenContent(
        modifier = modifier.fillMaxSize(),
        uiState = uiState,
        onTrainingPlanClicked = onTrainingPlanClicked,
        onSearchQueryChanged = viewModel::onSearchQueryChange,
        toggleCategory = viewModel::toggleCategory
    )
}

@Composable
private fun TrainingsScreenContent(
    modifier: Modifier = Modifier,
    uiState: TrainingsListUiState,
    onTrainingPlanClicked: (TrainingPlanId) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    toggleCategory: (ExerciseCategory) -> Unit
) {
    Column(modifier) {
        val filteringState = uiState.filteringState
        SearchHeader(
            query = filteringState.searchQuery,
            onSearchQueryChanged = onSearchQueryChanged,
            categories = filteringState.categories,
            toggleCategory = toggleCategory,
            label = stringResource(R.string.search_training_plan)
        )
        when (uiState) {
            is TrainingsListUiState.Loading -> LoadingScreen(
                modifier = Modifier.fillMaxSize(),
            )

            is TrainingsListUiState.Failure -> FailureScreen(
                modifier = Modifier.fillMaxSize(),
                title = stringResource(R.string.failed_to_load_training_plans),
                message = uiState.message ?: ""
            )

            is TrainingsListUiState.Success -> TrainingPlansOverviews(
                plans = uiState.plans,
                onTrainingPlanClicked = onTrainingPlanClicked
            )
        }
    }
}

@Composable
private fun TrainingPlansOverviews(
    modifier: Modifier = Modifier,
    plans: List<TrainingPlanOverview>,
    onTrainingPlanClicked: (TrainingPlanId) -> Unit,
) {
    if (plans.isNotEmpty()) {
        TrainingsList(
            modifier = modifier,
            plans = plans,
            onTrainingPlanClicked = onTrainingPlanClicked
        )
    } else {
        NoDataMessage(
            modifier = modifier,
            text = stringResource(id = R.string.no_trainings_info)
        )
    }
}


@Composable
private fun TrainingsList(
    modifier: Modifier = Modifier,
    plans: List<TrainingPlanOverview>,
    onTrainingPlanClicked: (TrainingPlanId) -> Unit,
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(plans, key = { it.id.toString() }) { trainingOverview ->
            TrainingPlanOverviewCard(
                trainingOverview = trainingOverview,
                onClick = { onTrainingPlanClicked(trainingOverview.id) }
            )
        }
    }
}


@Composable
private fun TrainingPlanOverviewCard(
    trainingOverview: TrainingPlanOverview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListCardItem(
        modifier = modifier,
        onCardClicked = onClick
    ) {
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(Dimens.normal)
        ) {
            Text(
                text = trainingOverview.title,
                style = MaterialTheme.typography.headlineMedium,
            )
            trainingOverview.description.takeIf { it.isNotEmpty() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            CategoriesRow(
                categories = trainingOverview.categories.map { it.name },
                modifier = Modifier.fillMaxWidth()
            )
            TrainingPlanDateInfo(
                instant = trainingOverview.lastModification,
                icon = Icons.Default.CalendarMonth,
                stringRes = R.string.last_modified
            )
            trainingOverview.lastSession?.let {
                TrainingPlanDateInfo(
                    instant = it,
                    icon = Icons.Default.PlayArrow,
                    stringRes = R.string.last_session
                )
            }
        }
    }
}

@Composable
fun TrainingPlanDateInfo(
    instant: Instant,
    icon: ImageVector,
    @StringRes stringRes: Int,
    modifier: Modifier = Modifier,
) {
    val color = MediumGrey
    val timeMillis = instant.toEpochMilliseconds()
    val nowMillis = System.currentTimeMillis()
    val formattedTime = DateUtils.getRelativeTimeSpanString(
        timeMillis,
        nowMillis,
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE
    )
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.padding(end = Dimens.normal),
            tint = color
        )
        Text(
            text = stringResource(stringRes, formattedTime),
            color = color,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview
@Composable
private fun TrainingsScreenLoadingPreview() {
    TrainingPlannerTheme {
        TrainingsScreenContent(
            uiState = TrainingsListUiState.Loading(
                filteringState = FilteringState("Some query", categories = emptyList())
            ),
            onTrainingPlanClicked = {},
            onSearchQueryChanged = {},
            toggleCategory = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
private fun TrainingsScreenFailurePreview() {
    TrainingPlannerTheme {
        TrainingsScreenContent(
            uiState = TrainingsListUiState.Failure(
                filteringState = FilteringState("Some query", categories = emptyList()),
                message = "Detailed failure message"
            ),
            onTrainingPlanClicked = {},
            onSearchQueryChanged = {},
            toggleCategory = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
private fun TrainingsScreenEmptyListPreview() {
    TrainingPlannerTheme {
        TrainingsScreenContent(
            uiState = TrainingsListUiState.Success(
                filteringState = FilteringState("Some query", categories = emptyList()),
                plans = emptyList()
            ),
            onTrainingPlanClicked = {},
            onSearchQueryChanged = {},
            toggleCategory = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
private fun TrainingsScreenSuccessPreview() {
    TrainingPlannerTheme {
        TrainingsScreenContent(
            uiState = TrainingsListUiState.Success(
                filteringState = FilteringState("Some query", categories = emptyList()),
                plans = PREVIEW_TRAINING_PLANS_OVERVIEWS
            ),
            onTrainingPlanClicked = {},
            onSearchQueryChanged = {},
            toggleCategory = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
private fun TrainingPlanOverviewCardPreview() {
    TrainingPlannerTheme {
        TrainingPlanOverviewCard(
            trainingOverview = PREVIEW_TRAINING_PLANS_OVERVIEWS.first(),
            onClick = {},
        )
    }
}

private val PREVIEW_TRAINING_PLANS_OVERVIEWS = listOf(
    TrainingPlanOverview(
        id = TrainingPlanId.create(),
        title = "Full Body Workout",
        description = "A complete full body workout targeting all major muscle groups",
        categories = setOf(
            ExerciseCategoryName("Chest"),
            ExerciseCategoryName("Back"),
            ExerciseCategoryName("Legs")
        ),
        lastModification = Instant.parse("2026-09-01T12:00:00Z"),
        lastSession = Instant.parse("2026-09-10T14:30:00Z")
    ),
    TrainingPlanOverview(
        id = TrainingPlanId.create(),
        title = "Upper Body Strength",
        description = "Focus on chest, back, shoulders and arms strength",
        categories = setOf(
            ExerciseCategoryName("Chest"),
            ExerciseCategoryName("Back"),
            ExerciseCategoryName("Shoulders"),
            ExerciseCategoryName("Arms")
        ),
        lastModification = Instant.parse("2026-09-05T10:00:00Z"),
        lastSession = null
    ),
    TrainingPlanOverview(
        id = TrainingPlanId.create(),
        title = "Cardio & Core",
        description = "High intensity cardio routine with core stability exercises",
        categories = setOf(
            ExerciseCategoryName("Cardio"),
            ExerciseCategoryName("Abs")
        ),
        lastModification = Instant.parse("2026-10-03T08:15:00Z"),
        lastSession = Instant.parse("2025-09-12T18:00:00Z")
    )
)


