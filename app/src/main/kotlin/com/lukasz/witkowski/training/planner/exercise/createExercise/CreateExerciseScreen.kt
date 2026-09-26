package com.lukasz.witkowski.training.planner.exercise.createExercise

import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.presentation.FilterCategory
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category
import com.lukasz.witkowski.training.planner.image.ImageBitmap
import com.lukasz.witkowski.training.planner.ui.components.CategoryFilters
import com.lukasz.witkowski.training.planner.ui.components.DropDownInput
import com.lukasz.witkowski.training.planner.ui.components.FormFieldLabel
import com.lukasz.witkowski.training.planner.ui.components.ImageContainer
import com.lukasz.witkowski.training.planner.ui.components.PREVIEW_CATEGORIES
import com.lukasz.witkowski.training.planner.ui.components.TextField
import com.lukasz.witkowski.training.planner.ui.theme.Dimens
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme

@Composable
fun CreateExerciseScreen(
    modifier: Modifier = Modifier,
    viewModel: ExerciseEditorViewModel,
    navigateUp: () -> Unit,
) {
    val state by viewModel.editingState.collectAsState()
    CreateExerciseScreenContent(
        modifier = modifier.fillMaxSize(),
        state = state,
    )
    /*
        val image by viewModel.image.collectAsState()
        val name: String by viewModel.name.collectAsState()
        val description by viewModel.description.collectAsState()
        val selectedCategory by viewModel.category.collectAsState()
        val savingState by viewModel.savingState.collectAsState()

        Scaffold(
            modifier = modifier,
        ) {
            when (savingState) {
                is ResultHandler.Idle, is ResultHandler.Error -> {
                    CreateExerciseForm(
                        image = image,
                        name = name,
                        description = description,
                        allCategories = viewModel.allCategories,
                        selectedCategory = selectedCategory,
                        onImageChange = { viewModel.onImageChange(it) },
                        onExerciseNameChanged = { viewModel.onExerciseNameChange(it) },
                        onExerciseDescriptionChanged = { viewModel.onExerciseDescriptionChange(it) },
                        onCategorySelected = { viewModel.onCategorySelected(it) }
                    )
                }
                is ResultHandler.Success -> {
                    LaunchedEffect(Unit) {
                        navigateUp()
                    }
                }
                else -> {
                    LoadingScreen(
                        modifier = Modifier.fillMaxSize().padding(it),
                        message = stringResource(id = R.string.saving_exercise, name)
                    )
                }
            }
        }

     */
}

@Composable
private fun CreateExerciseScreenContent(
    state: ExerciseEditingState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(Dimens.normal),
        verticalArrangement = Arrangement.spacedBy(Dimens.large)
    ) {
        ImageInputForm(
            images = state.images,
            previewImage = state.previewImage
        )
        TextInputForm(
            name = state.name,
            description = state.description,
            onUserInputChange = {},
        )
        CategorySelectionPanel(
            categories = state.categories,
            toggleCategory = {},
        )
    }
}

@Composable
private fun TextInputForm(
    name: String,
    description: String,
    onUserInputChange: (ExerciseEditingEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        FormFieldLabel(
            text = stringResource(R.string.exercise_name_label),
            modifier = Modifier.padding(bottom = Dimens.normal)
        )
        TextField(
            text = name,
            onTextChange = { onUserInputChange(ExerciseEditingEvent.NameChanged(it)) },
            label = stringResource(R.string.enter_exercise_name)
        )
        Spacer(Modifier.height(Dimens.large))
        FormFieldLabel(
            text = stringResource(R.string.exercise_description_label),
            modifier = Modifier.padding(bottom = Dimens.normal)
        )
        TextField(
            text = description,
            onTextChange = { onUserInputChange(ExerciseEditingEvent.DescriptionChanged(it)) },
            label = stringResource(R.string.enter_exercise_description),
            minLines = 3,
            maxLines = 5
        )
    }
}

@Composable
private fun CategorySelectionPanel(
    categories: List<FilterCategory>,
    toggleCategory: (ExerciseCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        FormFieldLabel(
            text = stringResource(R.string.target_muscles),
            modifier = Modifier.padding(bottom = Dimens.normal)
        )
        CategoryFilters(
            categories = categories,
            toggleCategory = toggleCategory
        )
    }
    
}

@Composable
private fun CreateExerciseForm(
    image: ImageBitmap?,
    name: String,
    description: String,
    allCategories: List<Category>,
    selectedCategory: Category,
    onImageChange: (Bitmap) -> Unit,
    onExerciseNameChanged: (String) -> Unit,
    onExerciseDescriptionChanged: (String) -> Unit,
    onCategorySelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UploadImageLayout(
            image = image?.bitmap,
            onImageChange = onImageChange
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            text = name,
            onTextChange = onExerciseNameChanged,
            label = stringResource(id = R.string.name),
            imeAction = ImeAction.Next
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            text = description,
            onTextChange = onExerciseDescriptionChanged,
            label = stringResource(id = R.string.description),
            imeAction = ImeAction.Done,
            maxLines = 5
        )
        Spacer(modifier = Modifier.height(16.dp))
        DropDownInput(
            selectedText = stringResource(id = selectedCategory.res),
            suggestions = allCategories.map { stringResource(id = it.res) },
            label = stringResource(id = R.string.category),
            onSuggestionSelected = onCategorySelected
        )
    }
}

@Composable
fun UploadImageLayout(
    modifier: Modifier = Modifier,
    image: Bitmap?,
    onImageChange: (Bitmap) -> Unit
) {
    val launcher = imageActivityResultLauncher(onImageChange = onImageChange)
    val placeholder: Int = R.drawable.exercise_default
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ImageContainer {
            AsyncImage(
                model = image ?: placeholder,
                contentDescription = null,
//                modifier = Modifier
//                    .width(200.dp)
//                    .height(200.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { launcher.launch("image/*") }
        ) {
            Text(text = stringResource(id = R.string.upload_image), color = Color.Black)
        }
    }
}

@Composable
private fun imageActivityResultLauncher(
    onImageChange: (Bitmap) -> Unit
): ManagedActivityResultLauncher<String, Uri?> {
    val context = LocalContext.current
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            val bitmap = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                MediaStore.Images
                    .Media.getBitmap(context.contentResolver, uri)
            } else {
                val source = ImageDecoder
                    .createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
            onImageChange(bitmap)
        }
    )
}

@Preview
@Composable
fun CreateExerciseScreenPreview() {
    TrainingPlannerTheme {
        val bitmaps = remember { createPreviewBitmaps() }
        val state = ExerciseEditingState(
            images = bitmaps,
            previewImage = bitmaps.first(),
            categories = PREVIEW_CATEGORIES
        )
        CreateExerciseScreenContent(
            modifier = Modifier.fillMaxSize(),
            state = state
        )
    }
}