package com.lukasz.witkowski.training.planner.ui.exercisesList

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.witkowski.shared.models.Category
import com.lukasz.witkowski.shared.models.Exercise
import com.lukasz.witkowski.shared.utils.categoriesWithoutNone
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.ui.components.CategoryChip
import com.lukasz.witkowski.training.planner.ui.components.ListCardItem

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
    pickedExercises: List<Exercise> = emptyList()
) {
    // TODO check why this screen is jumping when displayed
    val exercisesList by viewModel.exercises.observeAsState(initial = emptyList())
    val selectedCategoriesList by viewModel.selectedCategories.observeAsState(initial = emptyList())

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
            categories = categoriesWithoutNone,
            selectedCategories = selectedCategoriesList,
            selectCategory = { viewModel.selectCategory(it) }
        )
        ExercisesList(
            exercisesList = exercisesList,
            openDialog = {
                isExerciseDialogOpen = true
                exercise = it
            },
            pickingExerciseMode = pickingExerciseMode,
            pickExercise = pickExercise,
            pickedExercises = pickedExercises
        )
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
                text = category.name,
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
    pickedExercises: List<Exercise> = emptyList()
) {
    LazyColumn() {
        items(exercisesList) { exercise ->
            ListCardItem(modifier = Modifier,
                onCardClicked = {
                    if (pickingExerciseMode) {
                        pickExercise(exercise)
                    } else {
                        openDialog(exercise)
                    }
                }
            ) {
                ExerciseListItemContent(
                    exercise = exercise,
                    isHighlighted = pickedExercises.contains(exercise)
                )
            }
        }
    }
}

@Composable
fun ExerciseListItemContent(
    modifier: Modifier = Modifier,
    exercise: Exercise,
    isHighlighted: Boolean = false
) {
    val image = exercise.image
    val imageDescription = "${exercise.name} image"

    Row(
        modifier = if (isHighlighted) modifier.background(Color.Red) else modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ImageWithDefaultPlaceholder(imageDescription = imageDescription, image = image)

        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = exercise.name,
                fontSize = 24.sp
            )
            Text(
                text = exercise.description,
                fontSize = 14.sp
            )
            val category = exercise.category
            if (category != Category.None) {
                CategoryChip(
                    modifier = Modifier.padding(4.dp),
                    text = category.name
                )
            }

        }
    }
}

@Composable
private fun ImageWithDefaultPlaceholder(
    modifier: Modifier = Modifier,
    imageDescription: String,
    image: Bitmap?
) {
    val imageModifier = modifier
        .heightIn(min = 60.dp, max = 120.dp)
        .padding(4.dp)
        .clip(RoundedCornerShape(16.dp))
    if (image == null) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
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

@Composable
fun ExerciseInfoAlertDialog(
    modifier: Modifier = Modifier,
    exercise: Exercise,
    closeDialog: () -> Unit
) {

    AlertDialog(modifier = modifier
        .clip(MaterialTheme.shapes.medium)
        .border(width = 1.dp, Color.Yellow, MaterialTheme.shapes.medium)
        .fillMaxWidth(),
        onDismissRequest = closeDialog,
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = exercise.name,
                fontSize = 32.sp,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "",
                    modifier = Modifier.height(8.dp)
                ) // without Text here image is moving to the top
                ImageWithDefaultPlaceholder(
                    modifier = Modifier.height(240.dp),
                    imageDescription = "${exercise.name} image", image = exercise.image
                )
                Text(
                    text = exercise.description,
                    modifier = Modifier
                )
                if (exercise.category != Category.None)
                    CategoryChip(
                        modifier = Modifier.padding(top = 4.dp),
                        text = exercise.category.name
                    )
            }

        },
        buttons = {
            Row(horizontalArrangement = Arrangement.Center) {

            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                onClick = closeDialog
            ) {
                Text(text = "Ok")
            }
        }
    )

}
