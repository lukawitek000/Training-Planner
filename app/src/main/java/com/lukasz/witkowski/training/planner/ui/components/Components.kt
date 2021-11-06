package com.lukasz.witkowski.training.planner.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun ListCardItem(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .padding(4.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = Color.DarkGray
    ){
        Box(modifier = Modifier.padding(8.dp)){
            content()
        }
    }
}


@Composable
fun TextField(
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