package com.lukasz.witkowski.training.planner.user.presentation

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.button.AppPrimaryButton
import com.lukasz.witkowski.training.planner.shared.ui.button.AppSecondaryButton
import com.lukasz.witkowski.training.planner.shared.ui.button.AppTertiaryButton
import com.lukasz.witkowski.training.planner.user.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SignOutBottomSheet(
    onKeepLocalDataClick: () -> Unit,
    onRemoveAllLocalDataClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = BottomSheetDefaults.ExpandedShape,
        modifier = modifier.testTag("SignOutBottomSheet")
    ) {
        SignOutBottomSheetContent(
            onKeepLocalDataClick = onKeepLocalDataClick,
            onRemoveAllLocalDataClick = onRemoveAllLocalDataClick,
            onDismissRequest = onDismissRequest
        )
    }
}

@Composable
internal fun SignOutBottomSheetContent(
    onKeepLocalDataClick: () -> Unit,
    onRemoveAllLocalDataClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens2.margin)
            .padding(bottom = Dimens2.spaceLg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(Dimens2.spinnerIconContainerSize),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
            border = BorderStroke(
                width = Dimens2.thinBorder,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            )
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(Dimens2.iconSize30)
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens2.spaceSm))

        Text(
            text = stringResource(id = R.string.sign_out_dialog_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Text(
            text = stringResource(id = R.string.sign_out_dialog_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(max = Dimens2.maxSubtitleWidth + Dimens2.spaceXl)
                .padding(top = Dimens2.spaceXs)
        )

        Spacer(modifier = Modifier.height(Dimens2.spaceMd))

        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f),
            border = BorderStroke(
                width = Dimens2.thinBorder,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(Dimens2.spaceSm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(Dimens2.iconSize22)
                )
                Text(
                    text = stringResource(id = R.string.sign_out_warning_unsynced_lost),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens2.spaceLg))

        AppPrimaryButton(
            text = stringResource(id = R.string.sign_out_keep_local_button),
            onClick = onKeepLocalDataClick,
            leadingIcon = Icons.Default.Smartphone,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("SignOutKeepLocalButton")
        )

        Spacer(modifier = Modifier.height(Dimens2.spaceSm))

        AppSecondaryButton(
            text = stringResource(id = R.string.sign_out_remove_all_button),
            onClick = onRemoveAllLocalDataClick,
            leadingIcon = Icons.Default.DeleteForever,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("SignOutRemoveAllButton")
        )

        Spacer(modifier = Modifier.height(Dimens2.spaceXs))

        AppTertiaryButton(
            text = stringResource(id = R.string.sign_out_cancel_button),
            onClick = onDismissRequest,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("SignOutCancelButton")
        )
    }
}

@AppPreview
@Composable
private fun SignOutBottomSheetContentPreview() {
    TrainingPlannerTheme2 {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            SignOutBottomSheetContent(
                onKeepLocalDataClick = {},
                onRemoveAllLocalDataClick = {},
                onDismissRequest = {}
            )
        }
    }
}
