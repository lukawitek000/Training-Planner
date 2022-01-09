package com.lukasz.witkowski.training.planner.ui.createExercise

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Create
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.lukasz.witkowski.shared.models.Category
import com.lukasz.witkowski.shared.utils.allCategories
import com.lukasz.witkowski.training.planner.ui.components.ImageContainer
import com.lukasz.witkowski.training.planner.ui.components.TextField
import com.lukasz.witkowski.training.planner.ui.theme.LightDark12
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme
import com.skydoves.landscapist.glide.GlideImage


@Composable
fun CreateExerciseScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateExerciseViewModel,
    navigateBack: () -> Unit
) {
    val image by viewModel.image.observeAsState()
    val title: String by viewModel.title.observeAsState("")
    val description by viewModel.description.observeAsState(initial = "")
    val selectedCategory by viewModel.category.observeAsState(initial = Category.None)

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.createExercise()
                navigateBack()
            }) {
                Icon(imageVector = Icons.Default.Create, contentDescription = "Create exercise")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UploadImageButton(
                image = image,
                onImageChange = { viewModel.onImageChange(it) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                text = title,
                onTextChange = { viewModel.onExerciseNameChange(it) },
                label = "Title",
                imeAction = ImeAction.Next
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                text = description,
                onTextChange = { viewModel.onExerciseDescriptionChange(it) },
                label = "Description",
                imeAction = ImeAction.Done,
                maxLines = 5
            )
            Spacer(modifier = Modifier.height(16.dp))
            DropDownInput(
                selectedText = selectedCategory.name,
                suggestions = allCategories.map { it.name },
                label = "Category",
                onSuggestionSelected = { viewModel.onCategorySelected(it) }
            )
        }
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
        Uri.parse("android.resource://com.lukasz.witkowski.training.planner/drawable/ic_launcher_foreground")
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
        onClick = { launcher.launch("image/*") },
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Yellow)
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