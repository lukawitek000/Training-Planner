package com.lukasz.witkowski.training.planner.training.presentation.delete

import android.widget.Toast
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.dialogs.AppConfirmationDialog
import com.lukasz.witkowski.training.planner.shared.ui.loading.AppLoadingState
import com.lukasz.witkowski.training.planner.training.R
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DeleteTrainingPlanScreen(
    viewModel: DeleteTrainingPlanViewModel,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val successMessage = stringResource(R.string.training_plan_deleted)
    val failureMessage = stringResource(R.string.training_plan_deletion_failed)
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.deletionEvent.collectLatest { event ->
            val text = when (event) {
                DeleteTrainingPlanEvent.Failure -> failureMessage
                DeleteTrainingPlanEvent.Success -> successMessage
            }
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
            onDelete()
        }
    }

    val state by viewModel.state.collectAsState()
    DeleteTrainingPlanDialog(
        state = state,
        onDelete = { viewModel.deleteTrainingPlan() },
        onCancel = onCancel,
        modifier = modifier
    )
}

@Composable
fun DeleteTrainingPlanDialog(
    state: DeleteTrainingPlanUiState,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        DeleteTrainingPlanUiState.Loading -> {
            AppLoadingState(
                title = stringResource(R.string.loading_training_plan),
                modifier = modifier
            )
        }

        is DeleteTrainingPlanUiState.LoadedTrainingPlan -> {
            AppConfirmationDialog(
                title = stringResource(R.string.delete_training_plan_dialog_title),
                message = stringResource(R.string.delete_training_plan_dialog_text),
                onConfirm = onDelete,
                onDismiss = onCancel,
                isDestructive = true,
                dialogTestTag = "DeleteTrainingPlanDialog",
                titleTestTag = "DeleteTrainingPlanTitle",
                modifier = modifier
            )
        }

        is DeleteTrainingPlanUiState.Error -> {
            AppConfirmationDialog(
                title = stringResource(R.string.training_plan_deletion_failed),
                message = state.message,
                onConfirm = onCancel,
                onDismiss = onCancel,
                confirmText = stringResource(android.R.string.ok),
                showDismissButton = false,
                isDestructive = false,
                dialogTestTag = "DeleteTrainingPlanDialog",
                titleTestTag = "DeleteTrainingPlanTitle",
                modifier = modifier
            )
        }
    }
}

@AppPreview
@Composable
private fun DeleteTrainingPlanDialogPreview() {
    TrainingPlannerTheme2 {
        Surface {
            DeleteTrainingPlanDialog(
                state = DeleteTrainingPlanUiState.LoadedTrainingPlan(
                    title = "Full Body"
                ),
                onDelete = {},
                onCancel = {}
            )
        }
    }
}
