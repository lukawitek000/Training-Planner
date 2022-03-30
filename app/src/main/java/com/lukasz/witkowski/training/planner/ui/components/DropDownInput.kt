package com.lukasz.witkowski.training.planner.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.toSize


@Composable
fun DropDownInput(
    selectedText: String,
    suggestions: List<Int>,
    label: String,
    onSuggestionSelected: (Int) -> Unit
) {
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    var expanded by remember { mutableStateOf(false) }
    val icon = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown
    Column() {
        TextField(
            value = selectedText,
            onValueChange = { /*onSuggestionSelected(it)*/ },
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
                    Text(text = stringResource(id = it), color = MaterialTheme.colors.primary)
                }
            }
        }
    }
}
