package com.lukasz.witkowski.training.planner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lukasz.witkowski.training.planner.ui.theme.LightDark12
import com.lukasz.witkowski.training.planner.ui.theme.LightDark5
import com.lukasz.witkowski.training.planner.ui.theme.OrangeTransparent

@Composable
fun ListCardItem(
    modifier: Modifier = Modifier,
    onCardClicked: () -> Unit = {},
    onCardLongClicked: () -> Unit = {},
    markedSelected: Boolean = false,
    backgroundColor: Color = LightDark5,
    content: @Composable () -> Unit
) {
    val bgdColor = if (markedSelected) OrangeTransparent else backgroundColor
    val borderModifier = if (markedSelected) Modifier.border(
        2.dp,
        MaterialTheme.colors.primary,
        MaterialTheme.shapes.medium
    ) else Modifier
    Card(
        modifier = modifier
            .padding(4.dp)
            .fillMaxWidth()
            .then(borderModifier),
        backgroundColor = bgdColor
    ) {
        Box(modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onCardClicked() },
                    onLongPress = { onCardLongClicked() }
                )
            }
            .padding(8.dp)) {
            content()
        }
    }
}

@Composable
fun ImageContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(LightDark12)
            .padding(4.dp)
    ) {
        content()
    }
}


@Composable
fun TextField(
    modifier: Modifier = Modifier,
    text: String,
    onTextChange: (String) -> Unit,
    label: String,
    imeAction: ImeAction = ImeAction.Default,
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLines: Int = 1
) {
    val keyboardController = LocalFocusManager.current
    val fr = FocusRequester.Default
    TextField(
        value = text,
        onValueChange = onTextChange,
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(fr),
        label = {
            Text(text = label, color = MaterialTheme.colors.primaryVariant)
        },
        textStyle = TextStyle(color = MaterialTheme.colors.primary),
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(imeAction = imeAction, keyboardType = keyboardType),
        keyboardActions = KeyboardActions(
            onNext = { fr.requestFocus() },
            onDone = { keyboardController.clearFocus() }
        ),
    )
}

@Composable
fun NoDataMessage(modifier: Modifier = Modifier, text: String) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text)
    }
}
