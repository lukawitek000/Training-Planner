package com.lukasz.witkowski.training.planner.exercise.presentation.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.lukasz.witkowski.training.planner.exercise.R
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseMetadata
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseOwner
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSharing
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSource
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSyncState
import com.lukasz.witkowski.training.planner.exercise.presentation.FilteringState
import com.lukasz.witkowski.training.planner.exercise.presentation.PREVIEW_CATEGORIES
import com.lukasz.witkowski.training.planner.exercise.presentation.PREVIEW_PULL_UP_THUMBNAIL
import com.lukasz.witkowski.training.planner.exercise.presentation.PREVIEW_PUSH_UP_THUMBNAIL
import com.lukasz.witkowski.training.planner.exercise.presentation.components.AppExerciseImage
import com.lukasz.witkowski.training.planner.exercise.presentation.models.ExerciseOverview
import com.lukasz.witkowski.training.planner.shared.featureflags.FeatureFlag
import com.lukasz.witkowski.training.planner.shared.featureflags.LocalFeatureFlagProvider
import com.lukasz.witkowski.training.planner.shared.theme.Dimens
import com.lukasz.witkowski.training.planner.shared.theme.Info
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.theme.Warning
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.badges.AppMetadataPill
import com.lukasz.witkowski.training.planner.shared.ui.badges.AppStatusBadge
import com.lukasz.witkowski.training.planner.shared.ui.chips.AppCategoryPill
import com.lukasz.witkowski.training.planner.shared.ui.chips.AppCategoryPillStyle
import com.lukasz.witkowski.training.planner.shared.ui.chips.AppFilterChip
import com.lukasz.witkowski.training.planner.shared.ui.feedback.AppEmptyState
import com.lukasz.witkowski.training.planner.shared.ui.feedback.AppErrorState
import com.lukasz.witkowski.training.planner.shared.ui.inputField.AppSearchBar
import com.lukasz.witkowski.training.planner.shared.ui.loading.AppLoadingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun ExercisesScreen(
    viewModel: ExercisesListViewModel,
    onExerciseClicked: (ExerciseId) -> Unit,
    modifier: Modifier = Modifier
) {
    val exercisesList = viewModel.exercises.collectAsLazyPagingItems()
    val filteringState by viewModel.filteringState.collectAsState()
    ExercisesScreenContent(
        modifier = modifier.fillMaxSize(),
        exercisesList = exercisesList,
        filteringState = filteringState,
        toggleCategory = viewModel::toggleCategory,
        onSearchQueryChanged = viewModel::onSearchQueryChange,
        onExerciseClicked = onExerciseClicked,
        onClearCategories = viewModel::clearCategories,
        onResetFilters = viewModel::resetFilters
    )
}

@Composable
fun ExercisesScreenContent(
    exercisesList: LazyPagingItems<ExerciseOverview>,
    filteringState: FilteringState,
    toggleCategory: (ExerciseCategory) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onExerciseClicked: (ExerciseId) -> Unit,
    modifier: Modifier = Modifier,
    onClearCategories: () -> Unit = {},
    onResetFilters: () -> Unit = {
        onSearchQueryChanged("")
        onClearCategories()
    },
    itemTrailingIcon: (@Composable (ExerciseId) -> Unit)? = null
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
    ) {
        ExercisesSearchAndFilterHeader(
            filteringState = filteringState,
            onSearchQueryChanged = onSearchQueryChanged,
            toggleCategory = toggleCategory,
            onClearCategories = onClearCategories
        )

        when (val loadState = exercisesList.loadState.refresh) {
            is LoadState.Loading -> AppLoadingState(
                title = stringResource(R.string.loading_exercises),
                modifier = Modifier.fillMaxSize()
            )

            is LoadState.Error -> AppErrorState(
                title = stringResource(R.string.failed_to_load_exercises),
                subtitle = loadState.error.localizedMessage.orEmpty(),
                modifier = Modifier.fillMaxSize()
            )

            is LoadState.NotLoading -> if (exercisesList.itemCount != 0) {
                ExercisesList(
                    exercisesList = exercisesList,
                    onExerciseClicked = onExerciseClicked,
                    itemTrailingIcon = itemTrailingIcon,
                    modifier = Modifier.weight(1f)
                )
            } else {
                val hasActiveFilters =
                    filteringState.isAnyCategorySelected || filteringState.searchQuery.isNotBlank()
                AppEmptyState(
                    title =
                        if (filteringState.isAnyCategorySelected) {
                            stringResource(id = R.string.no_exercises_for_categories)
                        } else {
                            stringResource(id = R.string.no_exercises)
                        },
                    subtitle = stringResource(id = R.string.no_exercises_subtitle),
                    actionLabel = if (hasActiveFilters) {
                        stringResource(
                            id = R.string.reset_filters
                        )
                    } else {
                        null
                    },
                    onActionClick = if (hasActiveFilters) onResetFilters else null,
                    modifier =
                        Modifier
                            .weight(1f)
                            .testTag("NoDataMessage")
                )
            }
        }
    }
}

@Composable
private fun ExercisesSearchAndFilterHeader(
    filteringState: FilteringState,
    onSearchQueryChanged: (String) -> Unit,
    toggleCategory: (ExerciseCategory) -> Unit,
    onClearCategories: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = Dimens.spaceSm, bottom = Dimens.spaceSm),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
    ) {
        AppSearchBar(
            query = filteringState.searchQuery,
            onQueryChange = onSearchQueryChanged,
            placeholder = stringResource(R.string.search_exercise),
            modifier = Modifier.padding(horizontal = Dimens.margin)
        )

        if (filteringState.categories.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = Dimens.margin),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
            ) {
                item(key = "filter_all") {
                    AppFilterChip(
                        label = stringResource(R.string.filter_all),
                        selected = !filteringState.isAnyCategorySelected,
                        onClick = onClearCategories
                    )
                }
                items(
                    items = filteringState.categories,
                    key = { it.category.name }
                ) { filterCategory ->
                    AppFilterChip(
                        label = filterCategory.category.name,
                        selected = filterCategory.isSelected,
                        showCheckIcon = true,
                        onClick = { toggleCategory(filterCategory.category) },
                        modifier = Modifier.testTag(
                            "Chip${filterCategory.category.name}, isClickable=true"
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ExercisesList(
    exercisesList: LazyPagingItems<ExerciseOverview>,
    onExerciseClicked: (ExerciseId) -> Unit,
    modifier: Modifier = Modifier,
    itemTrailingIcon: (@Composable (ExerciseId) -> Unit)? = null
) {
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .testTag("ExercisesList"),
        contentPadding =
            PaddingValues(
                start = Dimens.margin,
                end = Dimens.margin,
                top = Dimens.spaceXs,
                bottom = Dimens.spaceXl
            ),
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
    ) {
        items(
            count = exercisesList.itemCount,
            key = exercisesList.itemKey { it.id.value }
        ) { index ->
            val exercise = exercisesList[index]
            if (exercise != null) {
                ExerciseListItemContent(
                    exercise = exercise,
                    onClick = { onExerciseClicked(exercise.id) },
                    itemTrailingIcon = itemTrailingIcon,
                    modifier = Modifier.testTag("ExerciseItem-${exercise.name}")
                )
            }
        }
    }
}

@Composable
fun ExerciseListItemContent(
    exercise: ExerciseOverview,
    onClick: (ExerciseId) -> Unit,
    modifier: Modifier = Modifier,
    itemTrailingIcon: (@Composable (ExerciseId) -> Unit)? = null
) {
    val isOnlineSyncEnabled = LocalFeatureFlagProvider.current.isEnabled(FeatureFlag.ONLINE_SYNC)
    val imageDescription = stringResource(id = R.string.image_description, exercise.name)
    val syncState = exercise.metadata.syncState
    val borderStroke =
        if (isOnlineSyncEnabled) {
            when (syncState) {
                ExerciseSyncState.CONFLICT ->
                    BorderStroke(Dimens.thinBorder, Warning.copy(alpha = 0.45f))

                ExerciseSyncState.ERROR ->
                    BorderStroke(
                        Dimens.thinBorder,
                        MaterialTheme.colorScheme.error.copy(alpha = 0.45f)
                    )

                else ->
                    BorderStroke(
                        Dimens.thinBorder,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                    )
            }
        } else {
            BorderStroke(
                Dimens.thinBorder,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            )
        }

    Surface(
        onClick = { onClick(exercise.id) },
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = borderStroke
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(Dimens.spaceSm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppExerciseImage(
                imageReference = exercise.thumbnail,
                contentDescription = imageDescription,
                modifier = Modifier
                    .weight(0.5f, fill = true)
                    .height(Dimens.listItemThumbnailSize)
            )
            Spacer(modifier = Modifier.width(Dimens.spaceMd))
            ExerciseInformation(
                modifier = Modifier.weight(1f),
                exercise = exercise,
                isOnlineSyncEnabled = isOnlineSyncEnabled
            )
            itemTrailingIcon?.let { icon ->
                Spacer(modifier = Modifier.width(Dimens.spaceSm))
                icon(exercise.id)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExerciseInformation(
    exercise: ExerciseOverview,
    isOnlineSyncEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val categories = exercise.categories
    val metadata = exercise.metadata

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier
                        .weight(1f)
                        .testTag("ExerciseItemName-${exercise.name}")
            )
            if (isOnlineSyncEnabled) {
                Spacer(modifier = Modifier.width(Dimens.spaceXs))
                ExerciseSyncBadge(syncState = metadata.syncState)
            }
        }

        if (categories.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceXs),
                verticalArrangement = Arrangement.spacedBy(Dimens.spaceXs)
            ) {
                categories.forEach { category ->
                    AppCategoryPill(
                        text = category.name,
                        style = AppCategoryPillStyle.Filled
                    )
                }
            }
        }
        ExerciseSourcePill(metadata = metadata)
    }
}

@Composable
internal fun ExerciseSyncBadge(syncState: ExerciseSyncState, modifier: Modifier = Modifier) {
    when (syncState) {
        ExerciseSyncState.CONFLICT -> {
            AppStatusBadge(
                text = stringResource(R.string.sync_status_conflict),
                toneColor = Warning,
                leadingIcon = Icons.Default.SyncProblem,
                modifier = modifier
            )
        }

        ExerciseSyncState.ERROR -> {
            AppStatusBadge(
                text = stringResource(R.string.sync_status_error),
                toneColor = MaterialTheme.colorScheme.error,
                leadingIcon = Icons.Default.ErrorOutline,
                modifier = modifier
            )
        }

        ExerciseSyncState.PENDING_UPLOAD, ExerciseSyncState.PENDING_DELETE -> {
            AppStatusBadge(
                text = stringResource(R.string.sync_status_pending),
                toneColor = Info,
                leadingIcon = Icons.Default.CloudSync,
                modifier = modifier
            )
        }

        ExerciseSyncState.LOCAL_ONLY -> {
            AppStatusBadge(
                text = stringResource(R.string.sync_status_local),
                toneColor = MaterialTheme.colorScheme.secondary,
                leadingIcon = Icons.Default.CloudOff,
                modifier = modifier
            )
        }

        ExerciseSyncState.SYNCED -> {
            // Keep list clean for synced items unless explicitly requested
        }
    }
}

@Composable
private fun ExerciseSourcePill(metadata: ExerciseMetadata, modifier: Modifier = Modifier) {
    when (val source = metadata.source) {
        ExerciseSource.Bundled -> {
            AppMetadataPill(
                text = stringResource(R.string.source_bundled),
                leadingIcon = Icons.Default.Storage,
                modifier = modifier
            )
        }

        is ExerciseSource.UserCreated -> {
            when (val owner = source.owner) {
                is ExerciseOwner.Local -> {
                    AppMetadataPill(
                        text = stringResource(R.string.source_owned_by_me),
                        leadingIcon = Icons.Default.Person,
                        contentColor = MaterialTheme.colorScheme.primaryContainer,
                        modifier = modifier
                    )
                }

                is ExerciseOwner.User -> {
                    val label =
                        if (metadata.sharing is ExerciseSharing.Community) {
                            owner.username.ifBlank { stringResource(R.string.source_community) }
                        } else {
                            stringResource(R.string.source_owned_by_me)
                        }
                    AppMetadataPill(
                        text = label,
                        leadingIcon =
                            if (metadata.sharing is ExerciseSharing.Community) {
                                Icons.Default.Public
                            } else {
                                Icons.Default.Cloud
                            },
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = modifier
                    )
                }
            }
        }

        ExerciseSource.Organization -> {
            AppMetadataPill(
                text = stringResource(R.string.source_organization),
                leadingIcon = Icons.Default.Public,
                modifier = modifier
            )
        }
    }
}

fun <T : Any> List<T>.asPreviewPagerFlow(): Flow<PagingData<T>> = flowOf(PagingData.from(this))

val PREVIEW_EXERCISES =
    listOf(
        ExerciseOverview(
            id = ExerciseId.create(),
            name = "Push ups",
            description = "Compound upper-body pushing movement",
            categories = listOf(ExerciseCategory("Chest"), ExerciseCategory("Shoulders")),
            thumbnail = PREVIEW_PUSH_UP_THUMBNAIL,
            metadata =
                ExerciseMetadata(
                    source = ExerciseSource.UserCreated(ExerciseOwner.Local()),
                    syncState = ExerciseSyncState.CONFLICT
                )
        ),
        ExerciseOverview(
            id = ExerciseId.create(),
            name = "Pull up",
            description = "Vertical pulling exercise for lats and biceps",
            categories = listOf(ExerciseCategory("Back"), ExerciseCategory("Biceps")),
            thumbnail = PREVIEW_PULL_UP_THUMBNAIL,
            metadata =
                ExerciseMetadata(
                    source = ExerciseSource.Bundled,
                    syncState = ExerciseSyncState.SYNCED
                )
        )
    )

@AppPreview
@Composable
private fun ExerciseListItemPreview() {
    TrainingPlannerTheme {
        Box(modifier = Modifier.padding(Dimens.spaceMd)) {
            ExerciseListItemContent(
                exercise = PREVIEW_EXERCISES.first(),
                onClick = {}
            )
        }
    }
}

@AppPreview
@Composable
private fun ExercisesScreenContentPreview() {
    TrainingPlannerTheme {
        ExercisesScreenContent(
            exercisesList = PREVIEW_EXERCISES.asPreviewPagerFlow().collectAsLazyPagingItems(),
            filteringState =
                FilteringState(
                    searchQuery = "",
                    categories = PREVIEW_CATEGORIES
                ),
            toggleCategory = {},
            onSearchQueryChanged = {},
            onExerciseClicked = {}
        )
    }
}
