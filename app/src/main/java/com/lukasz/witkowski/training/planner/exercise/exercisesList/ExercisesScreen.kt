package com.lukasz.witkowski.training.planner.exercise.exercisesList

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.witkowski.training.planner.DialogState
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise
import com.lukasz.witkowski.training.planner.ui.components.CategoryChip
import com.lukasz.witkowski.training.planner.ui.components.CategoryFilters
import com.lukasz.witkowski.training.planner.ui.components.CustomSnackbar
import com.lukasz.witkowski.training.planner.ui.components.EditDeleteDialog
import com.lukasz.witkowski.training.planner.ui.components.ImageContainer
import com.lukasz.witkowski.training.planner.ui.components.ListCardItem
import com.lukasz.witkowski.training.planner.ui.components.NoDataMessage
import kotlinx.coroutines.launch

@Composable
fun ExercisesScreen(
    modifier: Modifier = Modifier,
    viewModel: ExercisesListViewModel,
    navigateToExerciseCreateScreen: (ExerciseId?) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { navigateToExerciseCreateScreen(null) }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Exercise")
            }
        },
        isFloatingActionButtonDocked = true,
        bottomBar = {
            // TODO if user lefts the screen the exercise is not deleted
            SnackbarHost(hostState = snackbarHostState) {
                CustomSnackbar(snackbarData = it)
            }
        },
    ) {
        ExercisesScreenContent(
            viewModel = viewModel,
            onExerciseDeleted = {
                scope.launch {
                viewModel.removeExerciseFromView(it)
                    when(snackbarHostState.showSnackbar("Exercise ${it.name} deleted", actionLabel = "Undo")) {
                        SnackbarResult.Dismissed -> viewModel.deleteExercise(it)
                        SnackbarResult.ActionPerformed -> viewModel.undoDeleting(it)
                    }
                }
            },
            onExerciseEditedClicked = {
                navigateToExerciseCreateScreen(it.id)
            }
        )
    }
}

@Composable
fun ExercisesScreenContent(
    viewModel: ExercisesListViewModel,
    isPickingExerciseMode: Boolean = false,
    onExerciseClicked: (Exercise) -> Unit = {},
    onExerciseDeleted: (Exercise) -> Unit = {},
    onExerciseEditedClicked: (Exercise) -> Unit = {},
    pickedExercisesId: List<ExerciseId> = emptyList()
) {
    val exercisesList by viewModel.exercises.collectAsState(emptyList())
    val selectedCategoriesList by viewModel.selectedCategories.collectAsState()
    var exerciseDetailsDialogState by remember {
        mutableStateOf<DialogState<Exercise>>(DialogState.Closed())
    }
    var editDeleteDialogState by remember {
        mutableStateOf<DialogState<Exercise>>(DialogState.Closed())
    }

    exerciseDetailsDialogState.IsOpen {
        ExerciseInfoAlertDialog(
            exercise = it,
            closeDialog = { exerciseDetailsDialogState = DialogState.Closed() })
    }

    editDeleteDialogState.IsOpen {
        EditDeleteDialog(
            text = it.name,
            onEditClicked = {
                editDeleteDialogState = DialogState.Closed()
                onExerciseEditedClicked(it)
                            },
            onDeleteClicked = {
                editDeleteDialogState = DialogState.Closed()
                onExerciseDeleted(it)
            },
            onDismissRequest = { editDeleteDialogState = DialogState.Closed() }
        )
    }

    Column() {
        CategoryFilters(
            categories = viewModel.categoriesWithoutNone,
            selectedCategories = selectedCategoriesList,
            selectCategory = { viewModel.selectCategory(it) }
        )
        if (exercisesList.isNotEmpty()) {
            ExercisesList(
                exercisesList = exercisesList,
                onExerciseClicked = {
                    if (isPickingExerciseMode) {
                        onExerciseClicked(it)
                    } else {
                        exerciseDetailsDialogState = DialogState.Open(it)
                    }
                },
                onExerciseLongClicked = {
                    if (!isPickingExerciseMode) {
                        editDeleteDialogState = DialogState.Open(it)
                    }
                },
                pickedExercisesId = pickedExercisesId
            )
        } else {
            NoDataMessage(
                text = if (selectedCategoriesList.isEmpty()) {
                    stringResource(id = R.string.no_exercises)
                } else {
                    stringResource(id = R.string.no_exercises_for_categories)
                }
            )
        }
    }
}

@Composable
private fun ExercisesList(
    modifier: Modifier = Modifier,
    exercisesList: List<Exercise>,
    onExerciseClicked: (Exercise) -> Unit,
    onExerciseLongClicked: (Exercise) -> Unit,
    pickedExercisesId: List<ExerciseId> = emptyList()
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        LazyColumn {
            items(exercisesList) { exercise ->
                ListCardItem(
                    onCardClicked = {
                        onExerciseClicked(exercise)
                    },
                    onCardLongClicked = {
                        onExerciseLongClicked(exercise)
                    },
                    markedSelected = pickedExercisesId.contains(exercise.id)
                ) {
                    ExerciseListItemContent(
                        exercise = exercise
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseListItemContent(
    modifier: Modifier = Modifier,
    exercise: Exercise
) {
    val image = exercise.image
    val imageDescription = stringResource(id = R.string.image_description, exercise.name)
    val category = exercise.category

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ImageWithDefaultPlaceholder(
            imageDescription = imageDescription,
            image = image
        )
        Spacer(modifier = Modifier.width(16.dp))
        ExerciseInformation(
            modifier = Modifier,
            exercise = exercise,
            category = category
        )
    }
}

@Composable
private fun ExerciseInformation(
    modifier: Modifier = Modifier,
    exercise: Exercise,
    category: Category
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = exercise.name,
            fontSize = 28.sp
        )
        if (!category.isNone()) {
            Spacer(modifier = Modifier.height(16.dp))
        }
        CategoryChip(
            modifier = Modifier,
            category = category,
            fontSize = 14.sp
        )
    }
}

@Composable
fun ImageWithDefaultPlaceholder(
    modifier: Modifier = Modifier,
    imageDescription: String,
    image: Bitmap?,
    heightMin: Dp = 60.dp,
    heightMax: Dp = 120.dp
) {
    val imageModifier = modifier
        .heightIn(min = heightMin, max = heightMax)
        .padding(4.dp)
        .clip(RoundedCornerShape(16.dp))
    ImageContainer() {
        if (image == null) {
            Image(
                painter = painterResource(id = R.drawable.exercise_default),
                modifier = imageModifier,
                contentDescription = imageDescription
            )
        } else {
            Image(
                bitmap = image.asImageBitmap(),
                modifier = imageModifier,
                contentDescription = imageDescription
            )
        }
    }
}


