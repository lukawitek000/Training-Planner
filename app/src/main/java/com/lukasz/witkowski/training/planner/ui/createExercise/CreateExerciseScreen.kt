package com.lukasz.witkowski.training.planner.ui.createExercise

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.lukasz.witkowski.shared.models.Category
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun CreateExerciseScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateExerciseViewModel,
    navigateBack: () -> Unit
) {
    val title: String by viewModel.title.observeAsState("")
    val description by viewModel.description.observeAsState(initial = "")
    val suggestions = listOf(
        "ABS", "Back", "Legs", "Arms"
    )
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
            UploadImageButton()
            Spacer(modifier = Modifier.height(16.dp))
            TextField(title, { viewModel.onExerciseNameChange(it) }, "Title")
            Spacer(modifier = Modifier.height(16.dp))
            TextField(description, { viewModel.onExerciseDescriptionChange(it) }, "Description")
            Spacer(modifier = Modifier.height(16.dp))
            DropDownInput(
                selectedText = selectedCategory.name,
                suggestions = Category.allCategories.map { it.name },
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
private fun TextField(text: String, onTextChange: (String) -> Unit,label: String) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        label = {
            Text(text = label)
        },
        maxLines = 1,
        colors = TextFieldDefaults.textFieldColors(
            textColor = Color.Red,
            cursorColor = Color.Red,

            ),
        )
}

@Composable
fun UploadImageButton() {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->  imageUri = uri}
    )
    val placeholder = Uri.parse("android.resource://com.lukasz.witkowski.training.planner/drawable/ic_launcher_foreground")
    GlideImage(
        imageModel = imageUri ?: placeholder,
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