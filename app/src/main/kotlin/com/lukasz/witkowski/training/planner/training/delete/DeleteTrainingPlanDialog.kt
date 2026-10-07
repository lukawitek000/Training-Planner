package com.lukasz.witkowski.training.planner.training.delete

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.delete.DeletionEvent
import com.lukasz.witkowski.training.planner.ui.components.ConfirmDeleteDialog
import com.lukasz.witkowski.training.planner.ui.components.OverlayLoading
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme
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
                DeletionEvent.Failure -> failureMessage
                DeletionEvent.Success -> successMessage
            }
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
            onDelete()
        }
    }

    val state by viewModel.state.collectAsState()
    when (state) {
        DeleteTrainingPlanUiState.Loading -> OverlayLoading(modifier.fillMaxSize()) { }
        is DeleteTrainingPlanUiState.LoadedTrainingPlan -> DeleteTrainingPlanDialog(
            onDelete = {
                viewModel.deleteTrainingPlan()
            },
            onCancel = onCancel,
            modifier = modifier
        )
    }
}

@Composable
private fun DeleteTrainingPlanDialog(
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    ConfirmDeleteDialog(
        title = stringResource(R.string.delete_training_plan_dialog_title),
        message = stringResource(R.string.delete_training_plan_dialog_text),
        onDelete = onDelete,
        onCancel = onCancel,
        modifier = modifier,
        dialogTestTag = "DeleteTrainingPlanDialog",
        titleTestTag = "DeleteTrainingPlanTitle"
    )
}

@Composable
@Preview
private fun DeleteTrainingPlanDialogPreview() {
    TrainingPlannerTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            DeleteTrainingPlanDialog(
                onDelete = {},
                onCancel = {}
            )
        }
    }
}
