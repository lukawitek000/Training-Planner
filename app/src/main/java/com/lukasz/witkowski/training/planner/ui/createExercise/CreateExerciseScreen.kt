package com.lukasz.witkowski.training.planner.ui.createExercise

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
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
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.lukasz.witkowski.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.exercise.domain.Category
import com.lukasz.witkowski.training.planner.exercise.domain.allCategories
import com.lukasz.witkowski.training.planner.exercise.presentation.CreateExerciseViewModel
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
    val image by viewModel.image.observeAsState()
    val title: String by viewModel.title.observeAsState("")
    val description by viewModel.description.observeAsState(initial = "")
    val selectedCategory by viewModel.category.observeAsState(initial = Category.None)
    val savingState by viewModel.savingState.collectAsState()

    var showToast by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            if (savingState is ResultHandler.Idle) {
                FloatingActionButton(
                    onClick = {
                        if (title.isNotEmpty()) {
                            viewModel.createExercise()
                        } else {
                            showToast = true
                        }
                    },
                ) {
                    Icon(imageVector = Icons.Default.Create, contentDescription = "Create exercise")
                }
            }
        }
    ) {
        if (showToast) {
            Toast.makeText(LocalContext.current, "Exercise title is required", Toast.LENGTH_SHORT)
                .show()
            showToast = false
        }
        when (savingState) {
            is ResultHandler.Idle -> {
                CreateExerciseForm(
                    image = image,
                    title = title,
                    description = description,
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
private fun CreateExerciseForm(
    image: Bitmap?,
    title: String,
    description: String,
    selectedCategory: Category,
    onImageChange: (Bitmap) -> Unit,
    onExerciseNameChanged: (String) -> Unit,
    onExerciseDescriptionChanged: (String) -> Unit,
    onCategorySelected: (String) -> Unit

) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UploadImageButton(
            image = image,
            onImageChange = onImageChange
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            text = title,
            onTextChange = onExerciseNameChanged,
            label = "Title",
            imeAction = ImeAction.Next
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            text = description,
            onTextChange = onExerciseDescriptionChanged,
            label = "Description",
            imeAction = ImeAction.Done,
            maxLines = 5
        )
        Spacer(modifier = Modifier.height(16.dp))
        DropDownInput(
            selectedText = selectedCategory.name,
            suggestions = allCategories.map { it.name },
            label = "Category",
            onSuggestionSelected = onCategorySelected
        )
    }
}

@Composable
fun DropDownInput(
    selectedText: String,
    suggestions: List<String>,
    label: String,
    onSuggestionSelected: (String) -> Unit
) {
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    var expanded by remember { mutableStateOf(false) }
    val icon = if (expanded)
        Icons.Filled.ArrowDropUp
    else
        Icons.Filled.ArrowDropDown
    Column() {
        TextField(
            value = selectedText,
            onValueChange = { onSuggestionSelected(it) },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size.toSize()
                },
            label = { Text(text = label, color = MaterialTheme.colors.primaryVariant) },
            textStyle = TextStyle(color = MaterialTheme.colors.primary),
            trailingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = "Drop down arrow",
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            },
            readOnly = true
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(with(LocalDensity.current) { textFieldSize.width.toDp() })
        ) {
            suggestions.forEach {
                DropdownMenuItem(onClick = {
                    onSuggestionSelected(it)
                    expanded = !expanded
                }) {
                    Text(text = it, color = MaterialTheme.colors.primary)
                }
            }
        }
    }

}


@Composable
fun UploadImageButton(
    modifier: Modifier = Modifier,
    image: Bitmap?,
    onImageChange: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri == null) {
                return@rememberLauncherForActivityResult
            }
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
    val placeholder =
        Uri.parse("android.resource://com.lukasz.witkowski.training.planner/drawable/exercise_default")
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
        Text(text = "Upload image", color = Color.Black)
    }
}


@Preview
@Composable
fun CreateExerciseScreenPreview() {
    TrainingPlannerTheme {
        // CreateExerciseScreen()
    }
}