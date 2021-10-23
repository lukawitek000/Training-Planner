package com.lukasz.witkowski.training.planner.ui.createExercise

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Create
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.lukasz.witkowski.shared.models.Category
import com.lukasz.witkowski.shared.utils.allCategories
import com.lukasz.witkowski.training.planner.R
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
                imeAction = ImeAction.Next            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                text = description,
                onTextChange = { viewModel.onExerciseDescriptionChange(it) },
                label = "Description",
                imeAction = ImeAction.Done
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
    selectedText:  String,
    suggestions: List<String>,
    label: String,
    onSuggestionSelected: (String) -> Unit
) {
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    var expanded by remember { mutableStateOf(false) }
    val icon = if (expanded)
        Icons.Filled.ArrowDropUp //it requires androidx.compose.material:material-icons-extended
    else
        Icons.Filled.ArrowDropDown
    Column() {
        OutlinedTextField(
            value = selectedText,
            onValueChange = { onSuggestionSelected(it) },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size.toSize()
                },
            label = { Text(text = label) },
            trailingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = "Drop down arrow",
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            },
            readOnly = true
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(with(LocalDensity.current) { textFieldSize.width.toDp() } )
        ) {
            suggestions.forEach {
                DropdownMenuItem(onClick = {
                    onSuggestionSelected(it)
                    expanded = !expanded
                }) {
                    Text(text = it)
                }
            }
        }
    }

}

@Composable
private fun TextField(
    modifier: Modifier = Modifier,
    text: String,
    onTextChange: (String) -> Unit,
    label: String,
    imeAction: ImeAction = ImeAction.Default
) {
    val keyboardController = LocalFocusManager.current
    val fr = FocusRequester.Default
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        modifier = modifier.focusRequester(fr),
        label = {
            Text(text = label)
        },
        maxLines = 1,
        colors = TextFieldDefaults.textFieldColors(
            textColor = Color.Red,
            cursorColor = Color.Red,

            ),
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onNext = { fr.requestFocus() },
            onDone = { keyboardController.clearFocus() }
        ),
        )
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
    val placeholder = Uri.parse("android.resource://com.lukasz.witkowski.training.planner/drawable/ic_launcher_foreground")
    GlideImage(
        imageModel = image ?: placeholder,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .width(200.dp)
            .height(200.dp)
            .border(BorderStroke(2.dp, Color.Black))
    )
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