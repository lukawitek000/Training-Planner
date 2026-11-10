package com.lukasz.witkowski.training.planner.session.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.lukasz.witkowski.training.planner.session.R
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.dialogs.AppConfirmationDialog

@Composable
fun EndEarlySessionButton(onEndClicked: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onEndClicked,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "End training session",
            tint = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun EndEarlySessionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        AppConfirmationDialog(
            title = stringResource(R.string.session_exit_dialog_title),
            message = stringResource(R.string.session_exit_dialog_message),
            confirmText = stringResource(R.string.session_exit_dialog_confirm),
            dismissText = stringResource(R.string.session_exit_dialog_dismiss),
            isDestructive = true,
            onConfirm = {
                // TODO: Stop foreground service and cancel ongoing notifications before exiting session
                onConfirm()
            },
            onDismiss = onDismiss
        )
    }
}

@AppPreview
@Composable
fun EndEarlySessionButtonPreview() {
    TrainingPlannerTheme {
        EndEarlySessionButton(
            onEndClicked = {}
        )
    }
}

@AppPreview
@Composable
fun EndEarlySessionDialogPreview() {
    TrainingPlannerTheme {
        EndEarlySessionDialog(
            onDismiss = {},
            onConfirm = {}
        )
    }
}
