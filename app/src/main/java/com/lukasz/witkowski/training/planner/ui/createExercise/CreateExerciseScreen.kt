package com.lukasz.witkowski.training.planner.ui.createExercise

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme

@Composable
fun CreateExerciseScreen(viewModel: CreateExerciseViewModel) {
    val title: String by viewModel.title.observeAsState("")
    val description by viewModel.description.observeAsState(initial = "")
    val selectedCategory = remember { mutableStateOf("") }
    val suggestions = listOf(
        "ABS", "Back", "Legs", "Arms"
    )
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
        DropDownInput(selectedText = selectedCategory, suggestions = suggestions, label = "Category")
    }
}

@Composable
fun DropDownInput(
    selectedText:  MutableState<String>,
    suggestions: List<String>,
    label: String
) {
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    var expanded by remember { mutableStateOf(false) }
    val icon = if (expanded)
        Icons.Filled.ArrowDropUp //it requires androidx.compose.material:material-icons-extended
    else
        Icons.Filled.ArrowDropDown
    Column() {
        OutlinedTextField(
            value = selectedText.value,
            onValueChange = { selectedText.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size.toSize()
                },
            label = { Text(text = label) },
            trailingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = "Drop down categories",
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
                    selectedText.value = it
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
            textColor = Color.Yellow,
            cursorColor = Color.Yellow,

            ),
        )
}

@Composable
fun UploadImageButton() {
    Button(
        onClick = { Log.i("TAG", "UploadImageButton: TODO") },
        modifier = Modifier.size(200.dp),
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