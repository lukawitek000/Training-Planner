package com.lukasz.witkowski.training.planner.shared.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.lukasz.witkowski.training.planner.shared.R
import com.lukasz.witkowski.training.planner.shared.theme.Dimens
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview

/**
 * Stitch-styled modal confirmation dialog card (`RoundedCornerShape(Dimens.bottomSheetCornerRadius)`).
 *
 * Spec Highlights:
 * - Container: `RoundedCornerShape(Dimens.bottomSheetCornerRadius)` (28dp), `surfaceContainerHigh`, max width `Dimens.dialogMaxWidth` (360dp), `Dimens.spaceLg` padding.
 * - Nested circular icon badge: outer `Dimens.spinnerIconContainerSize` (64dp) `surfaceContainerHighest` circle, inner `Dimens.listItemThumbnailSize` (48dp) circle tinted with `errorContainer.copy(alpha = 0.4f)` (or `primaryContainer.copy(alpha = 0.25f)` when non-destructive).
 * - Title: centered `headlineSmall` bold text in `onSurface`.
 * - Description: centered `bodyMedium` text in `onSurfaceVariant`, max width `Dimens.maxSubtitleWidth`.
 * - Action Buttons Row: 2-column button row with Cancel (`surfaceContainerHighest` bg, `primary` text) and Confirm/Destructive (`error` bg + `onError` text when destructive, or `primaryContainer` bg + `onPrimary` text).
 */
@Composable
fun AppConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = stringResource(R.string.delete),
    dismissText: String = stringResource(R.string.cancel),
    icon: ImageVector = Icons.Default.Delete,
    iconContentDescription: String? = null,
    isDestructive: Boolean = true,
    showDismissButton: Boolean = true,
    dialogTestTag: String = "DeleteDialog",
    titleTestTag: String = "DeleteTitle",
    confirmButtonTestTag: String = "confirmButton",
    dismissButtonTestTag: String = "dismissButton"
) {
    val accentColor =
        if (isDestructive) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.primaryContainer
        }
    val innerBadgeColor =
        if (isDestructive) {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
        } else {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        }
    val confirmContentColor =
        if (isDestructive) {
            MaterialTheme.colorScheme.onError
        } else {
            MaterialTheme.colorScheme.onPrimary
        }

    Surface(
        modifier =
        modifier
            .widthIn(max = Dimens.dialogMaxWidth)
            .fillMaxWidth()
            .testTag(dialogTestTag),
        shape = RoundedCornerShape(Dimens.bottomSheetCornerRadius),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = Dimens.shadowLg
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceLg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Nested Circular Icon Badge
            Surface(
                modifier = Modifier.size(Dimens.spinnerIconContainerSize),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shadowElevation = Dimens.shadowSm
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier =
                        Modifier
                            .size(Dimens.listItemThumbnailSize)
                            .background(
                                color = innerBadgeColor,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = iconContentDescription,
                            tint = accentColor,
                            modifier = Modifier.size(Dimens.iconSize24)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spaceMd))

            // Centered Heading
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier =
                Modifier
                    .fillMaxWidth()
                    .testTag(titleTestTag)
            )

            Spacer(modifier = Modifier.height(Dimens.spaceXs))

            // Description Body
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = Dimens.maxSubtitleWidth)
            )

            Spacer(modifier = Modifier.height(Dimens.spaceXl))

            // 2-Column Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceMd)
            ) {
                if (showDismissButton) {
                    Button(
                        onClick = onDismiss,
                        modifier =
                        Modifier
                            .weight(1f)
                            .height(Dimens.buttonHeight)
                            .testTag(dismissButtonTestTag),
                        shape = CircleShape,
                        colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = dismissText,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Button(
                    onClick = onConfirm,
                    modifier =
                    Modifier
                        .weight(1f)
                        .height(Dimens.buttonHeight)
                        .testTag(confirmButtonTestTag),
                    shape = CircleShape,
                    colors =
                    ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = confirmContentColor
                    ),
                    elevation =
                    ButtonDefaults.buttonElevation(
                        defaultElevation = Dimens.buttonElevation
                    )
                ) {
                    Text(
                        text = confirmText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@AppPreview
@Composable
private fun AppConfirmationDialogDestructivePreview() {
    TrainingPlannerTheme {
        Box(modifier = Modifier.padding(Dimens.spaceMd)) {
            AppConfirmationDialog(
                title = "Delete exercise?",
                message =
                "This will permanently remove this exercise and its custom configurations. " +
                    "Training plans containing this exercise and past session logs will " +
                    "retain historical data.",
                onConfirm = {},
                onDismiss = {},
                isDestructive = true
            )
        }
    }
}

@AppPreview
@Composable
private fun AppConfirmationDialogStandardPreview() {
    TrainingPlannerTheme {
        Box(modifier = Modifier.padding(Dimens.spaceMd)) {
            AppConfirmationDialog(
                title = "Overwrite local changes?",
                message =
                "Accepting the cloud version will replace your local edits with the latest " +
                    "server revision.",
                confirmText = "Confirm",
                icon = Icons.Default.Warning,
                onConfirm = {},
                onDismiss = {},
                isDestructive = false
            )
        }
    }
}
