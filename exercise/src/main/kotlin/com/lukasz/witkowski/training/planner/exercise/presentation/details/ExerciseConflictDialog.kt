package com.lukasz.witkowski.training.planner.exercise.presentation.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.lukasz.witkowski.training.planner.exercise.R
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseMetadata
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseSyncState
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise2
import com.lukasz.witkowski.training.planner.exercise.presentation.models.ExerciseDetails
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Recommendation
import com.lukasz.witkowski.training.planner.exercise.presentation.models.RecommendationLevel
import com.lukasz.witkowski.training.planner.exercise.presentation.models.RecommendedParameters
import com.lukasz.witkowski.training.planner.shared.R as SharedR
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.theme.Warning
import com.lukasz.witkowski.training.planner.shared.ui.badges.AppMetadataPill
import com.lukasz.witkowski.training.planner.shared.ui.button.AppPrimaryButton
import com.lukasz.witkowski.training.planner.shared.ui.button.AppSecondaryButton
import com.lukasz.witkowski.training.planner.shared.ui.button.AppTertiaryButton
import com.lukasz.witkowski.training.planner.shared.ui.chips.AppCategoryPill
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseConflictDialog(
    localDetails: ExerciseDetails,
    serverDetails: ExerciseDetails?,
    onKeepLocal: () -> Unit,
    onAcceptServer: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
            .widthIn(max = Dimens2.dialogMaxWidthWide)
            .testTag("ExerciseConflictDialog")
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(
                width = Dimens2.thinBorder,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens2.spaceLg),
                verticalArrangement = Arrangement.spacedBy(Dimens2.spaceMd)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = Warning.copy(alpha = 0.14f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Warning,
                            modifier = Modifier
                                .padding(Dimens2.spaceSm)
                                .size(Dimens2.iconSize24)
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.conflict_dialog_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.conflict_dialog_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
                ) {
                    ConflictVersionColumn(
                        title = stringResource(R.string.conflict_local_version),
                        icon = Icons.Default.Smartphone,
                        accentColor = MaterialTheme.colorScheme.primary,
                        details = localDetails,
                        modifier = Modifier.weight(1f)
                    )
                    if (serverDetails != null) {
                        ConflictVersionColumn(
                            title = stringResource(R.string.conflict_cloud_version),
                            icon = Icons.Default.Cloud,
                            accentColor = Warning,
                            details = serverDetails,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
                ) {
                    AppPrimaryButton(
                        text = stringResource(R.string.conflict_keep_local),
                        onClick = onKeepLocal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("KeepLocalButton")
                    )
                    AppSecondaryButton(
                        text = stringResource(R.string.conflict_accept_cloud),
                        onClick = onAcceptServer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("AcceptServerButton")
                    )
                    AppTertiaryButton(
                        text = stringResource(SharedR.string.cancel),
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .testTag("DecideLaterButton")
                    )
                }
            }
        }
    }
}

@Composable
private fun ConflictVersionColumn(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    details: ExerciseDetails,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            width = Dimens2.thinBorder,
            color = accentColor.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(Dimens2.spaceSm),
            verticalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceXs)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(Dimens2.iconSize16)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            AppMetadataPill(
                text = stringResource(R.string.revision_badge, details.exercise.metadata.revision)
            )

            Text(
                text = details.exercise.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (details.exercise.description.isNotBlank()) {
                Text(
                    text = details.exercise.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (details.exercise.categories.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceXs),
                    verticalArrangement = Arrangement.spacedBy(Dimens2.spaceXs)
                ) {
                    details.exercise.categories.forEach { category ->
                        AppCategoryPill(text = category.name)
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens2.spaceXs))
            details.recommendations.forEach { rec ->
                val p = rec.parameters
                if (p.areValid()) {
                    val holdText = if (p.time != null &&
                        p.time.inWholeSeconds > 0
                    ) {
                        " · ${p.time.inWholeSeconds}s hold"
                    } else {
                        ""
                    }
                    Text(
                        text = "${rec.level.name.take(
                            3
                        )}: ${p.sets}x${p.reps}$holdText · ${p.restTime?.inWholeSeconds ?: 0}s",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ExerciseConflictDialogPreview() {
    TrainingPlannerTheme2 {
        val sampleRecs = listOf(
            Recommendation(
                RecommendationLevel.BEGINNER,
                RecommendedParameters(sets = 3, reps = 10, restTime = 60.seconds, weightInKg = 40)
            )
        )
        ExerciseConflictDialog(
            localDetails = ExerciseDetails(
                exercise = Exercise2(
                    id = ExerciseId.create(),
                    name = "Barbell Bench Press (Heavy)",
                    description = "Updated local notes with pause at bottom",
                    categories = listOf(ExerciseCategory("Chest"), ExerciseCategory("Triceps")),
                    metadata = ExerciseMetadata(
                        syncState = ExerciseSyncState.CONFLICT,
                        revision = 3
                    )
                ),
                recommendations = sampleRecs
            ),
            serverDetails = ExerciseDetails(
                exercise = Exercise2(
                    id = ExerciseId.create(),
                    name = "Barbell Bench Press",
                    description = "Cloud description updated from another device",
                    categories = listOf(ExerciseCategory("Chest")),
                    metadata = ExerciseMetadata(syncState = ExerciseSyncState.SYNCED, revision = 4)
                ),
                recommendations = sampleRecs
            ),
            onKeepLocal = {},
            onAcceptServer = {},
            onDismiss = {}
        )
    }
}
