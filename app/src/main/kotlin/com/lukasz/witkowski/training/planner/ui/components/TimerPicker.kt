package com.lukasz.witkowski.training.planner.ui.components

import android.view.LayoutInflater
import android.widget.NumberPicker
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.viewinterop.AndroidView
import com.lukasz.witkowski.training.planner.R


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
    val timerPickerModifier = if(isTimePickerEnabled) modifier else modifier.alpha(0.2f)
    if(!isTimePickerEnabled) {
        onMinutesChange(0)
        onSecondsChange(0)
    }
    AndroidView(
        modifier = timerPickerModifier,
        factory = { context ->
            val view = LayoutInflater.from(context).inflate(R.layout.timer_time_picker, null)
            view
        },
        update = { view ->
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
        }
    )
}