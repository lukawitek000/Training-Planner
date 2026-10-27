package com.lukasz.witkowski.training.planner.exercise.presentation.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import com.lukasz.witkowski.training.planner.exercise.R
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseMetadata
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseOwner
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSource
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSyncState
import com.lukasz.witkowski.training.planner.exercise.presentation.PREVIEW_PUSH_UP_IMAGES
import com.lukasz.witkowski.training.planner.image.presentation.ImageGallery
import com.lukasz.witkowski.training.planner.exercise.presentation.editor.PREVIEW_RECOMMENDED_PARAMETERS
import com.lukasz.witkowski.training.planner.exercise.presentation.editor.RecommendedParametersOverview
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise2
import com.lukasz.witkowski.training.planner.exercise.presentation.models.ExerciseDetails
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.Info
import com.lukasz.witkowski.training.planner.shared.theme.Success
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.theme.Warning
import com.lukasz.witkowski.training.planner.shared.ui.badges.AppMetadataPill
import com.lukasz.witkowski.training.planner.shared.ui.badges.AppStatusBadge
import com.lukasz.witkowski.training.planner.shared.ui.button.AppSecondaryButton
import com.lukasz.witkowski.training.planner.shared.ui.chips.AppCategoryPill
import com.lukasz.witkowski.training.planner.shared.ui.feedback.AppErrorState

@Composable
fun ExerciseDetailsScreen(viewModel: ExerciseDetailsViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsState()
    val showConflictDialog by viewModel.showConflictDialog.collectAsState()
    val conflictServerDetails by viewModel.conflictServerDetails.collectAsState()

    ExerciseDetailsScreenContent(
        state = state,
        modifier = modifier.fillMaxSize(),
        onResolveConflictClick = viewModel::openConflictDialog,
        onRetrySyncClick = viewModel::retrySync
    )

    val currentState = state
    if (showConflictDialog && currentState is ExerciseDetailsState.Success) {
        ExerciseConflictDialog(
            localDetails = currentState.details,
            serverDetails = conflictServerDetails,
            onKeepLocal = viewModel::resolveConflictKeepLocal,
            onAcceptServer = viewModel::resolveConflictAcceptServer,
            onDismiss = viewModel::dismissConflictDialog
        )
    }
}

@Composable
fun ExerciseDetailsScreenContent(
    state: ExerciseDetailsState,
    modifier: Modifier = Modifier,
    onResolveConflictClick: () -> Unit = {},
    onRetrySyncClick: () -> Unit = {},
    footer: @Composable ColumnScope.(ExerciseDetails) -> Unit = { details ->
        RecommendedParametersOverview(
            recommendations = details.recommendations
        )
    }
) {
    when (state) {
        ExerciseDetailsState.Loading -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .testTag("loading"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = stringResource(R.string.loading_exercise_details),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        is ExerciseDetailsState.Failure -> {
            AppErrorState(
                title = stringResource(R.string.failed_load_exercise_details),
                subtitle = state.message,
                modifier = modifier
            )
        }

        is ExerciseDetailsState.Success -> {
            ExerciseDetailsBody(
                details = state.details,
                onResolveConflictClick = onResolveConflictClick,
                onRetrySyncClick = onRetrySyncClick,
                footer = footer,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ExerciseDetailsBody(
    details: ExerciseDetails,
    onResolveConflictClick: () -> Unit,
    onRetrySyncClick: () -> Unit,
    footer: @Composable ColumnScope.(ExerciseDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    val exercise = details.exercise
    val metadata = exercise.metadata
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens2.spaceMd)
                .testTag("ExerciseDetails"),
            verticalArrangement = Arrangement.spacedBy(Dimens2.spaceMd)
        ) {
            if (exercise.images.isNotEmpty()) {
                ImageGallery(
                    images = exercise.images,
                    modifier = Modifier.padding(bottom = Dimens2.spaceSm)
                )
            }

            Text(
                text = exercise.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (exercise.categories.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceSm),
                    verticalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
                ) {
                    exercise.categories.forEach { category ->
                        AppCategoryPill(
                            text = category.name,
                            modifier = Modifier.testTag("Chip${category.name}, isClickable=false")
                        )
                    }
                }
            }

            ExerciseSyncStatusCard(metadata = metadata)

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(
                    width = Dimens2.thinBorder,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(Dimens2.spaceMd),
                    verticalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
                ) {
                    Text(
                        text = stringResource(R.string.description),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = exercise.description.ifBlank { stringResource(R.string.not_set) },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            footer(details)

            if (metadata.syncState == ExerciseSyncState.CONFLICT ||
                metadata.syncState == ExerciseSyncState.ERROR
            ) {
                Spacer(modifier = Modifier.height(Dimens2.spaceXl * 3))
            }
        }

        when (metadata.syncState) {
            ExerciseSyncState.CONFLICT -> {
                SyncActionBottomBanner(
                    title = stringResource(R.string.conflict_banner_title),
                    subtitle = stringResource(R.string.conflict_banner_subtitle),
                    actionLabel = stringResource(R.string.conflict_resolve_action),
                    isWarning = true,
                    onActionClick = onResolveConflictClick,
                    actionTestTag = "ResolveConflictButton",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(Dimens2.spaceMd)
                )
            }

            ExerciseSyncState.ERROR -> {
                SyncActionBottomBanner(
                    title = stringResource(R.string.sync_error_banner_title),
                    subtitle = stringResource(R.string.sync_error_banner_subtitle),
                    actionLabel = stringResource(R.string.sync_retry_action),
                    isWarning = false,
                    onActionClick = onRetrySyncClick,
                    actionTestTag = "RetrySyncButton",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(Dimens2.spaceMd)
                )
            }

            else -> Unit
        }
    }
}

@Composable
private fun ExerciseSyncStatusCard(metadata: ExerciseMetadata, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ExerciseSyncStatusCard"),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            width = Dimens2.thinBorder,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(Dimens2.spaceMd),
            verticalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.sync_status_synced),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                DetailsSyncBadge(syncState = metadata.syncState)
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceSm),
                verticalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
            ) {
                when (metadata.source) {
                    ExerciseSource.Bundled -> {
                        AppMetadataPill(
                            text = stringResource(R.string.source_bundled),
                            leadingIcon = Icons.Default.Shield
                        )
                        AppMetadataPill(
                            text = stringResource(R.string.bundled_read_only_badge),
                            leadingIcon = Icons.Default.Lock
                        )
                    }

                    is ExerciseSource.UserCreated -> {
                        AppMetadataPill(
                            text = stringResource(R.string.source_owned_by_me),
                            leadingIcon = Icons.Default.Person
                        )
                    }

                    ExerciseSource.Organization -> {
                        AppMetadataPill(
                            text = stringResource(R.string.source_organization),
                            leadingIcon = Icons.Default.CloudDone
                        )
                    }
                }

                AppMetadataPill(
                    text = stringResource(R.string.revision_badge, metadata.revision)
                )
            }
        }
    }
}

@Composable
private fun DetailsSyncBadge(syncState: ExerciseSyncState) {
    when (syncState) {
        ExerciseSyncState.SYNCED -> AppStatusBadge(
            text = stringResource(R.string.sync_status_synced),
            toneColor = Success,
            leadingIcon = Icons.Default.CloudDone
        )

        ExerciseSyncState.PENDING_UPLOAD,
        ExerciseSyncState.PENDING_DELETE -> AppStatusBadge(
            text = stringResource(R.string.sync_status_pending),
            toneColor = Info,
            leadingIcon = Icons.Default.CloudSync
        )

        ExerciseSyncState.CONFLICT -> AppStatusBadge(
            text = stringResource(R.string.sync_status_conflict),
            toneColor = Warning,
            leadingIcon = Icons.Default.Warning
        )

        ExerciseSyncState.ERROR -> AppStatusBadge(
            text = stringResource(R.string.sync_status_error),
            toneColor = MaterialTheme.colorScheme.error,
            leadingIcon = Icons.Default.ErrorOutline
        )

        ExerciseSyncState.LOCAL_ONLY -> AppStatusBadge(
            text = stringResource(R.string.sync_status_local),
            toneColor = MaterialTheme.colorScheme.secondary,
            leadingIcon = Icons.Default.CloudOff
        )
    }
}

@Composable
private fun SyncActionBottomBanner(
    title: String,
    subtitle: String,
    actionLabel: String,
    isWarning: Boolean,
    onActionClick: () -> Unit,
    actionTestTag: String,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isWarning) {
        Warning.copy(alpha = 0.14f)
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val accentColor = if (isWarning) {
        Warning
    } else {
        MaterialTheme.colorScheme.error
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("SyncActionBottomBanner"),
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
        border = BorderStroke(width = Dimens2.thinBorder, color = accentColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens2.spaceMd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
        ) {
            Icon(
                imageVector = if (isWarning) Icons.Default.Warning else Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(Dimens2.iconSize24)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AppSecondaryButton(
                text = actionLabel,
                onClick = onActionClick,
                modifier = Modifier.testTag(actionTestTag)
            )
        }
    }
}

@Composable
@Preview
private fun ExerciseDetailsScreenContentPreview() {
    TrainingPlannerTheme2 {
        Surface {
            ExerciseDetailsScreenContent(
                state = ExerciseDetailsState.Success(
                    details = ExerciseDetails(
                        exercise = Exercise2(
                            id = ExerciseId.create(),
                            name = "Push ups",
                            description = "Some long description in here",
                            categories = listOf(
                                ExerciseCategory("Chest"),
                                ExerciseCategory("Shoulder")
                            ),
                            images = PREVIEW_PUSH_UP_IMAGES,
                            metadata = ExerciseMetadata(
                                source = ExerciseSource.UserCreated(ExerciseOwner.Local()),
                                syncState = ExerciseSyncState.CONFLICT,
                                revision = 3
                            )
                        ),
                        recommendations = PREVIEW_RECOMMENDED_PARAMETERS
                    )
                ),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
@Preview
private fun ExerciseDetailsScreenLoadingPreview() {
    TrainingPlannerTheme2 {
        ExerciseDetailsScreenContent(
            state = ExerciseDetailsState.Loading,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
@Preview
private fun ExerciseDetailsScreenFailurePreview() {
    TrainingPlannerTheme2 {
        ExerciseDetailsScreenContent(
            state = ExerciseDetailsState.Failure("No exercise in DB"),
            modifier = Modifier.fillMaxSize()
        )
    }
}
