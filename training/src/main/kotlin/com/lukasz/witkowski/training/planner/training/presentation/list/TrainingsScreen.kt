package com.lukasz.witkowski.training.planner.training.presentation.list

import android.text.format.DateUtils
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.presentation.FilterCategory
import com.lukasz.witkowski.training.planner.exercise.presentation.FilteringState
import com.lukasz.witkowski.training.planner.shared.theme.Dimens
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.card.AppCard
import com.lukasz.witkowski.training.planner.shared.ui.chips.AppCategoryPill
import com.lukasz.witkowski.training.planner.shared.ui.chips.AppCategoryPillStyle
import com.lukasz.witkowski.training.planner.shared.ui.chips.AppFilterChip
import com.lukasz.witkowski.training.planner.shared.ui.feedback.AppEmptyState
import com.lukasz.witkowski.training.planner.shared.ui.feedback.AppErrorState
import com.lukasz.witkowski.training.planner.shared.ui.inputField.AppSearchBar
import com.lukasz.witkowski.training.planner.shared.ui.loading.AppLoadingState
import com.lukasz.witkowski.training.planner.training.R
import com.lukasz.witkowski.training.planner.training.domain.ExerciseCategoryName
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanOverview
import kotlin.time.Instant

@Composable
fun TrainingsScreen(
    viewModel: TrainingsListViewModel,
    onTrainingPlanClicked: (TrainingPlanId) -> Unit,
    modifier: Modifier = Modifier,
    onCreateTrainingClicked: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    TrainingsScreenContent(
        modifier = modifier.fillMaxSize(),
        uiState = uiState,
        onTrainingPlanClicked = onTrainingPlanClicked,
        onSearchQueryChanged = viewModel::onSearchQueryChange,
        toggleCategory = viewModel::toggleCategory,
        onCreateTrainingClicked = onCreateTrainingClicked
    )
}

@Composable
fun TrainingsScreenContent(
    uiState: TrainingsListUiState,
    onTrainingPlanClicked: (TrainingPlanId) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    toggleCategory: (ExerciseCategory) -> Unit,
    modifier: Modifier = Modifier,
    onCreateTrainingClicked: (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        val filteringState = uiState.filteringState
        AppSearchBar(
            query = filteringState.searchQuery,
            onQueryChange = onSearchQueryChanged,
            placeholder = stringResource(R.string.search_training_plan),
            modifier = Modifier.padding(horizontal = Dimens.spaceMd, vertical = Dimens.spaceSm)
        )

        if (filteringState.categories.isNotEmpty()) {
            CategoryFilters(
                categories = filteringState.categories,
                toggleCategory = toggleCategory,
                modifier = Modifier.padding(bottom = Dimens.spaceSm)
            )
        }

        when (uiState) {
            is TrainingsListUiState.Loading -> AppLoadingState(
                title = stringResource(R.string.loading_training_plan),
                modifier = Modifier.fillMaxSize()
            )

            is TrainingsListUiState.Failure -> AppErrorState(
                title = stringResource(R.string.failed_to_load_training_plans),
                subtitle = uiState.message.orEmpty(),
                modifier = Modifier.fillMaxSize()
            )

            is TrainingsListUiState.Success -> TrainingPlansOverviews(
                plans = uiState.plans,
                onTrainingPlanClicked = onTrainingPlanClicked,
                onCreateTrainingClicked = onCreateTrainingClicked,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun CategoryFilters(
    categories: List<FilterCategory>,
    toggleCategory: (ExerciseCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = Dimens.spaceMd),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
    ) {
        items(categories, key = { it.category.name }) { filterCategory ->
            AppFilterChip(
                label = filterCategory.category.name,
                selected = filterCategory.isSelected,
                onClick = { toggleCategory(filterCategory.category) }
            )
        }
    }
}

@Composable
private fun TrainingPlansOverviews(
    plans: List<TrainingPlanOverview>,
    onTrainingPlanClicked: (TrainingPlanId) -> Unit,
    modifier: Modifier = Modifier,
    onCreateTrainingClicked: (() -> Unit)? = null
) {
    if (plans.isNotEmpty()) {
        TrainingsList(
            modifier = modifier,
            plans = plans,
            onTrainingPlanClicked = onTrainingPlanClicked
        )
    } else {
        AppEmptyState(
            title = stringResource(R.string.no_trainings_info),
            actionLabel = onCreateTrainingClicked?.let { stringResource(R.string.create_training) },
            onActionClick = onCreateTrainingClicked,
            modifier = modifier
        )
    }
}

@Composable
private fun TrainingsList(
    plans: List<TrainingPlanOverview>,
    onTrainingPlanClicked: (TrainingPlanId) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(Dimens.spaceMd),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
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
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(Dimens.spaceMd),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
        ) {
            Text(
                text = trainingOverview.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (trainingOverview.description.isNotEmpty()) {
                Text(
                    text = trainingOverview.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (trainingOverview.categories.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spaceXs),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spaceXs)
                ) {
                    trainingOverview.categories.forEach { category ->
                        AppCategoryPill(
                            text = category.name,
                            style = AppCategoryPillStyle.Tonal
                        )
                    }
                }
            }
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
    modifier: Modifier = Modifier
) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
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
            modifier = Modifier.padding(end = Dimens.spaceXs),
            tint = color
        )
        Text(
            text = stringResource(stringRes, formattedTime),
            color = color,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@AppPreview
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

@AppPreview
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

@AppPreview
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

@AppPreview
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
    )
)
