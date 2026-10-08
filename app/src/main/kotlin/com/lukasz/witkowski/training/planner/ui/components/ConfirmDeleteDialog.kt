package com.lukasz.witkowski.training.planner.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.ui.theme.Dimens
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme

sealed interface ConfirmDeleteUiState {
    data object Loading : ConfirmDeleteUiState
    data class Loaded(
        val title: String,
        val message: String
    ) : ConfirmDeleteUiState

    data class Error(
        val title: String,
        val message: String
    ) : ConfirmDeleteUiState
}

@Composable
fun ConfirmDeleteDialog(
    state: ConfirmDeleteUiState,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    dialogTestTag: String = "DeleteDialog",
    titleTestTag: String = "DeleteTitle",
    confirmButtonTestTag: String = "confirmButton",
    dismissButtonTestTag: String = "dismissButton",
) {
    Surface(
        modifier = modifier.testTag(dialogTestTag),
        shape = RoundedCornerShape(Dimens.xlarge),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = Dimens.sligthlySmall,
    ) {
        Box(
            modifier = Modifier
                .padding(Dimens.slightlyLarge)
                .fillMaxWidth()
                .heightIn(min = Dimens.dialogMinHeight),
            contentAlignment = Alignment.Center,
        ) {
            when (state) {
                ConfirmDeleteUiState.Loading -> {
                    Box(
                        modifier = Modifier.testTag("OverlayContent"),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.testTag("Loading")
                        )
                    }
                }

                is ConfirmDeleteUiState.Loaded -> {
                    ConfirmDeleteContent(
                        title = state.title,
                        message = state.message,
                        onDelete = onDelete,
                        onCancel = onCancel,
                        titleTestTag = titleTestTag,
                        confirmButtonTestTag = confirmButtonTestTag,
                        dismissButtonTestTag = dismissButtonTestTag,
                    )
                }

                is ConfirmDeleteUiState.Error -> {
                    ConfirmDeleteErrorContent(
                        title = state.title,
                        message = state.message,
                        onDismiss = onCancel,
                        titleTestTag = titleTestTag,
                        dismissButtonTestTag = dismissButtonTestTag,
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfirmDeleteContent(
    title: String,
    message: String,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    titleTestTag: String,
    confirmButtonTestTag: String,
    dismissButtonTestTag: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.dialogIconBackgroundSize)
                .background(color = MaterialTheme.colorScheme.errorContainer, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(Dimens.dialogIconSize),
            )
        }

        Spacer(modifier = Modifier.height(Dimens.large))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(titleTestTag),
        )

        Spacer(modifier = Modifier.height(Dimens.slightlyLarge))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(Dimens.slightlyLarge))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.large),
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .testTag(dismissButtonTestTag),
                shape = CircleShape,
                border = BorderStroke(Dimens.thinBorder, MaterialTheme.colorScheme.primary),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    fontWeight = FontWeight.Bold,
                )
            }

            Button(
                onClick = onDelete,
                modifier = Modifier
                    .weight(1f)
                    .testTag(confirmButtonTestTag),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = Color.Black,
                ),
            ) {
                Text(
                    text = stringResource(R.string.delete),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun ConfirmDeleteErrorContent(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    titleTestTag: String,
    dismissButtonTestTag: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.dialogIconBackgroundSize)
                .background(color = MaterialTheme.colorScheme.errorContainer, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(Dimens.dialogIconSize),
            )
        }

        Spacer(modifier = Modifier.height(Dimens.large))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(titleTestTag),
        )

        Spacer(modifier = Modifier.height(Dimens.slightlyLarge))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(Dimens.slightlyLarge))

        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(dismissButtonTestTag),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(
                text = stringResource(R.string.done),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Preview
@Composable
private fun ConfirmDeleteDialogPreview() {
    TrainingPlannerTheme {
        ConfirmDeleteDialog(
            state = ConfirmDeleteUiState.Loaded(
                title = "Delete training plan?",
                message = "This will permanently delete this training plan and all its configuration. Past training sessions will be kept.",
            ),
            onDelete = {},
            onCancel = {},
            dialogTestTag = "dialogTestTag",
            titleTestTag = "titleTestTag",
            confirmButtonTestTag = "confirmButtonTestTag",
            dismissButtonTestTag = "dismissButtonTestTag",
            modifier = Modifier,
        )
    }
}

@Preview
@Composable
private fun ConfirmDeleteDialogLoadingPreview() {
    TrainingPlannerTheme {
        ConfirmDeleteDialog(
            state = ConfirmDeleteUiState.Loading,
            onDelete = {},
            onCancel = {},
        )
    }
}

@Preview
@Composable
private fun ConfirmDeleteDialogErrorPreview() {
    TrainingPlannerTheme {
        ConfirmDeleteDialog(
            state = ConfirmDeleteUiState.Error("Error", "Could not load item to delete"),
            onDelete = {},
            onCancel = {},
        )
    }
}
