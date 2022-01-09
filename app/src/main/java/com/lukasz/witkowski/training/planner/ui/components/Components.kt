package com.lukasz.witkowski.training.planner.ui.components

import android.view.LayoutInflater
import android.widget.NumberPicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.material.TextField
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.ui.theme.LightDark12
import com.lukasz.witkowski.training.planner.ui.theme.LightDark5

@Composable
fun ListCardItem(
    modifier: Modifier = Modifier,
    onCardClicked: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .padding(4.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = LightDark5
    ) {
        Box(modifier = Modifier
            .clickable {
                onCardClicked()
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
fun TimerTimePicker(
    modifier: Modifier = Modifier,
    minutes: Int,
    seconds: Int,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit,
    isTimePickerEnabled: Boolean = true,
    minutesMin: Int = 0,
    minutesMax: Int = 60,
    secondsMin: Int = 0,
    secondsMax: Int = 59,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val view = LayoutInflater.from(context).inflate(R.layout.timer_time_picker, null)
            val minutesPicker = view.findViewById<NumberPicker>(R.id.minutes_picker)
            val secondsPicker = view.findViewById<NumberPicker>(R.id.seconds_picker)
            minutesPicker.minValue = minutesMin
            minutesPicker.maxValue = minutesMax
            secondsPicker.minValue = secondsMin
            secondsPicker.maxValue = secondsMax
            minutesPicker.value = minutes
            secondsPicker.value = seconds
            minutesPicker.isEnabled = isTimePickerEnabled
            secondsPicker.isEnabled = isTimePickerEnabled
            minutesPicker.setOnValueChangedListener { _, _, newVal ->
                onMinutesChange(newVal)
            }
            secondsPicker.setOnValueChangedListener { _, _, newVal ->
                onSecondsChange(newVal)
            }
            view
        }
    )
}

@Composable
fun NoDataMessage(modifier: Modifier, text: String) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text)
    }
}