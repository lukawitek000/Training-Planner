package com.lukasz.witkowski.training.planner.exercise.exercisesList

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.models.Category
import com.lukasz.witkowski.training.planner.exercise.models.Exercise
import com.lukasz.witkowski.training.planner.ui.components.CategoryChip
import com.lukasz.witkowski.training.planner.ui.components.CategoryFilters
import com.lukasz.witkowski.training.planner.ui.components.DialogContainer
import com.lukasz.witkowski.training.planner.ui.components.ImageContainer
import com.lukasz.witkowski.training.planner.ui.components.ListCardItem
import com.lukasz.witkowski.training.planner.ui.components.NoDataMessage

@Composable
fun ExercisesScreen(
    modifier: Modifier = Modifier,
    viewModel: ExercisesListViewModel,
    onCreateExerciseFabClicked: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = { onCreateExerciseFabClicked() }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Exercise")
            }
        }
    ) {
        ExercisesScreenContent(viewModel)
    }
}

@Composable
fun ExercisesScreenContent(
    viewModel: ExercisesListViewModel,
    isPickingExerciseMode: Boolean = false,
    onExerciseClicked: (Exercise) -> Unit = {},
    pickedExercisesId: List<String> = emptyList()
) {
    val exercisesList by viewModel.exercises.collectAsState(emptyList())
    val selectedCategoriesList by viewModel.selectedCategories.collectAsState()

    var isExerciseDialogOpen by remember { mutableStateOf(false) }
    var exercise by remember {
        mutableStateOf(exercisesList.firstOrNull())
    }
    if (isExerciseDialogOpen && exercise != null) {
        ExerciseInfoAlertDialog(
            exercise = exercise!!,
            closeDialog = { isExerciseDialogOpen = false })
    }
    Column() {
        CategoryFilters(
            categories = viewModel.filterCategories,
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
                        isExerciseDialogOpen = true
                        exercise = it
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
    pickedExercisesId: List<String> = emptyList()
) {
    LazyColumn(modifier = modifier) {
        items(exercisesList) { exercise ->
            ListCardItem(
                modifier = Modifier,
                onCardClicked = {
                    onExerciseClicked(exercise)
                },
                markedSelected = pickedExercisesId.contains(exercise.id.value)
            ) {
                ExerciseListItemContent(
                    exercise = exercise
                )
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


