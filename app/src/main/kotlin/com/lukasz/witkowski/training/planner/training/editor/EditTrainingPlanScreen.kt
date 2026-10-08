package com.lukasz.witkowski.training.planner.training.editor

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun EditTrainingPlanScreen(
    viewModel: TrainingPlanEditorViewModel,
    onAddExerciseClicked: () -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TrainingPlanEditorScreen(
        viewModel = viewModel,
        onAddExerciseClicked = onAddExerciseClicked,
        navigateBack = navigateBack,
        modifier = modifier
    )
}
