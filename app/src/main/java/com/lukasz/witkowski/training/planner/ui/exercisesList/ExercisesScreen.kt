package com.lukasz.witkowski.training.planner.ui.exercisesList

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.Category
import com.lukasz.witkowski.training.planner.exercise.Exercise
import com.lukasz.witkowski.training.planner.exercise.ExercisesListViewModel
import com.lukasz.witkowski.training.planner.ui.components.CategoryChip
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
    pickingExerciseMode: Boolean = false,
    pickExercise: (Exercise) -> Unit = {},
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
            categories = viewModel.allCategories,
            selectedCategories = selectedCategoriesList,
            selectCategory = { viewModel.selectCategory(it) }
        )
        if (exercisesList.isNotEmpty()) {
            ExercisesList(
                exercisesList = exercisesList,
                openDialog = {
                    isExerciseDialogOpen = true
                    exercise = it
                },
                pickingExerciseMode = pickingExerciseMode,
                pickExercise = pickExercise,
                pickedExercisesId = pickedExercisesId
            )
        } else {
            NoDataMessage(
                modifier = Modifier,
                text = if (selectedCategoriesList.isEmpty()) "No exercises. Create your first exercise." else "No exercises for selected categories."
            )
        }
    }
}

@Composable
fun CategoryFilters(
    modifier: Modifier = Modifier,
    categories: List<Category>,
    selectedCategories: List<Category>,
    selectCategory: (Category) -> Unit
) {
    LazyRow(
        modifier = modifier.padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(categories) { category ->
            CategoryChip(
                modifier = Modifier.padding(4.dp),
                isSelected = selectedCategories.any { it == category },
                text = stringResource(id = category.res),
                selectionChanged = { selectCategory(category) },
                isClickable = true
            )
        }
    }
}

@Composable
private fun ExercisesList(
    modifier: Modifier = Modifier,
    exercisesList: List<Exercise>,
    openDialog: (Exercise) -> Unit,
    pickingExerciseMode: Boolean = false,
    pickExercise: (Exercise) -> Unit = {},
    pickedExercisesId: List<String> = emptyList()
) {
    LazyColumn(modifier = modifier) {
        items(exercisesList) { exercise ->
            ListCardItem(
                modifier = Modifier,
                onCardClicked = {
                    if (pickingExerciseMode) {
                        pickExercise(exercise)
                    } else {
                        openDialog(exercise)
                    }
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

@Composable
fun ExerciseListItemContent(
    modifier: Modifier = Modifier,
    exercise: Exercise
) {
    val image = exercise.image
    val imageDescription = "${exercise.name} image"
    val category = exercise.category

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ImageWithDefaultPlaceholder(imageDescription = imageDescription, image = image)

        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = exercise.name,
                fontSize = 28.sp
            )

            if (!category.isNone()) {
                Spacer(modifier = Modifier.height(16.dp))
                CategoryChip(
                    modifier = Modifier,
                    text = stringResource(id = category.res),
                    fontSize = 14.sp
                )
            }

        }
    }
}

@Composable
private fun ImageWithDefaultPlaceholder(
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

@Composable
fun ExerciseInfoAlertDialog(
    modifier: Modifier = Modifier,
    exercise: Exercise,
    closeDialog: () -> Unit
) {

    DialogContainer(modifier = modifier,
        closeDialog = closeDialog,
        saveData = {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = exercise.name,
                fontSize = 32.sp,
                color = MaterialTheme.colors.primary
            )
            Divider(Modifier.padding(8.dp), color = MaterialTheme.colors.primary)
            ImageWithDefaultPlaceholder(
                modifier = Modifier,
                imageDescription = "${exercise.name} image", image = exercise.image,
                heightMax = 350.dp
            )
            if (exercise.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = exercise.description,
                    modifier = Modifier
                )
            }
            val category = exercise.category
            if (!category.isNone()) {
                Spacer(modifier = Modifier.height(16.dp))
                CategoryChip(
                    modifier = Modifier.padding(top = 4.dp),
                    text = stringResource(id = category.res)
                )
            }
        }
    }
}

