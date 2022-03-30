package com.lukasz.witkowski.training.planner.exercise.createExercise

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Create
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.models.Category
import com.lukasz.witkowski.training.planner.ui.components.DropDownInput
import com.lukasz.witkowski.training.planner.ui.components.ImageContainer
import com.lukasz.witkowski.training.planner.ui.components.LoadingScreen
import com.lukasz.witkowski.training.planner.ui.components.TextField
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun CreateExerciseScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateExerciseViewModel,
    navigateBack: (String) -> Unit
) {
    val image by viewModel.image.collectAsState()
    val name: String by viewModel.name.collectAsState()
    val description by viewModel.description.collectAsState()
    val selectedCategory by viewModel.category.collectAsState()
    val savingState by viewModel.savingState.collectAsState()
    var showToast by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            CreateExerciseFloatingActionButton(
                savingState = savingState,
                name = name,
                createExercise = { viewModel.createExercise() },
                showToast = { showToast = true }
            )
        }
    ) {
        if (showToast) {
            Toast.makeText(LocalContext.current, stringResource(id = R.string.exercise_name_is_required), Toast.LENGTH_SHORT)
                .show()
            showToast = false
        }
        when (savingState) {
            is ResultHandler.Idle -> {
                CreateExerciseForm(
                    image = image,
                    name = name,
                    description = description,
                    allCategories = viewModel.getAllCategories(),
                    selectedCategory = selectedCategory,
                    onImageChange = { viewModel.onImageChange(it) },
                    onExerciseNameChanged = { viewModel.onExerciseNameChange(it) },
                    onExerciseDescriptionChanged = { viewModel.onExerciseDescriptionChange(it) },
                    onCategorySelected = { viewModel.onCategorySelected(it) }
                )
            }
            else -> {
                LoadingScreen(
                    modifier = Modifier.fillMaxSize(),
                    message = "Exercise is saving to the database"
                )
                if (savingState is ResultHandler.Loading) return@Scaffold
                val message = if (savingState is ResultHandler.Error) {
                    "Saving exercise failed"
                } else {
                    "Exercise saved"
                }
                LaunchedEffect(Unit) {
                    navigateBack(message)
                }
            }
        }
    }
}

@Composable
private fun CreateExerciseFloatingActionButton(
    modifier: Modifier = Modifier,
    savingState: ResultHandler<Boolean>,
    name: String,
    createExercise: () -> Unit,
    showToast: () -> Unit
) {
    if (savingState is ResultHandler.Idle) {
        FloatingActionButton(
            modifier = modifier,
            onClick = {
                if (name.isNotEmpty()) {
                    createExercise()
                } else {
                    showToast()
                }
            },
        ) {
            Icon(imageVector = Icons.Default.Create, contentDescription = "Create exercise")
        }
    }
}

@Composable
private fun CreateExerciseForm(
    image: Bitmap?,
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
            image = image,
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
    val placeholder =
        Uri.parse("android.resource://com.lukasz.witkowski.training.planner/drawable/exercise_default")
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ImageContainer {
            GlideImage(
                imageModel = image ?: placeholder,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(200.dp)
                    .height(200.dp)
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
):  ManagedActivityResultLauncher<String, Uri?> {
    val context = LocalContext.current
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
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
        // CreateExerciseScreen()
    }
}