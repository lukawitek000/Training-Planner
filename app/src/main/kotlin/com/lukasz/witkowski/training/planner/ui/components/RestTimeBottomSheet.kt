package com.lukasz.witkowski.training.planner.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.times
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.ui.theme.Dimens
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import timber.log.Timber
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestTimeBottomSheet(
    onDismissRequest: () -> Unit,
    onRestTimeConfigured: (Duration) -> Unit,
    defaultRestTime: Duration,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        RestTimeBottomSheetContent(
            onConfirmClicked = onRestTimeConfigured,
            restTime = defaultRestTime
        )

    }
}

@Composable
private fun RestTimeBottomSheetContent(
    onConfirmClicked: (Duration) -> Unit,
    restTime: Duration,
    modifier: Modifier = Modifier,
) {
    val restTimes = remember(restTime) { buildRestTimes(restTime) }
    var selectedIndex by remember { mutableIntStateOf(restTimes.indexOfFirst { it.selectedValue.value != null }) }
    val selectedRestTime = restTimes[selectedIndex]
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimens.normal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.normal)
    ) {
        Text(
            text = stringResource(R.string.select_rest_time),
            style = MaterialTheme.typography.titleLarge
        )
        TimeUnitSelectionButton(
            options = restTimes.map { it.unitOption },
            selectedOption = selectedRestTime.unitOption,
            onSelectOption = { selectedIndex = it }
        )
        if (selectedRestTime is RestTime.PresetValues) {
            PresetValuesGrid(
                presetValues = selectedRestTime
            )
        } else if (selectedRestTime is RestTime.Custom) {
            CustomTimePicker(
                customRestTime = selectedRestTime
            )
        }
        ConfirmButton(
            text = stringResource(R.string.done),
            onClick = {
                selectedRestTime.selectedValue.value?.let { onConfirmClicked(it) }
            },
            isEnabled = selectedRestTime.selectedValue.value != null
        )
    }
}



@Composable
private fun PresetValuesGrid(
    presetValues: RestTime.PresetValues,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.large, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(Dimens.large),
        maxItemsInEachRow = 3,
    ) {
        presetValues.forEachIndexed { index, value, isSelected ->
            val background = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
            val shape = RoundedCornerShape(Dimens.normal)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(2f)
                        .border(
                            Dimens.thinBorder,
                            MaterialTheme.colorScheme.outline,
                            shape = shape
                        )
                        .clickable {
                            presetValues.select(index)
                        }
                        .background(background, shape)
                        .padding(Dimens.large),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(presetValues.suffixStringRes, value),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

        }

    }
}

@Composable
private fun TimeUnitSelectionButton(
    options: List<TimeUnitOption>,
    selectedOption: TimeUnitOption,
    onSelectOption: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
    ) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                onClick = { onSelectOption(index) },
                selected = option == selectedOption,
                label = {
                    Text(
                        text = stringResource(option.labelRes)
                    )
                },
                icon = {},
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primary,
                    activeContentColor = MaterialTheme.colorScheme.onPrimary,
                )
            )
        }
    }
}

@Composable
private fun CustomTimePicker(
    customRestTime: RestTime.Custom,
    modifier: Modifier = Modifier
) {
    val minutesList = remember { (0..60).toList() }
    val secondsList = remember { listOf(0, 15, 30, 45) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.padding(bottom = Dimens.normal),
            horizontalArrangement = Arrangement.spacedBy(Dimens.dotsSeparatorSize + 2 * Dimens.large)
        ) {
            Text(
                text = stringResource(R.string.minutes),
                modifier = Modifier.width(Dimens.wheelWidth),
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.seconds),
                modifier = Modifier.width(Dimens.wheelWidth),
                textAlign = TextAlign.Center
            )
        }
        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PickerWheelColumn(
                items = minutesList,
                initialItem = customRestTime.minutes.intValue,
                formatText = { "$it" },
                onItemSelected = {
                    Timber.i("Minutes item selected $it")
                    customRestTime.minutes.intValue = it
                }
            )

            MiddleDotsSeparator(
                modifier = Modifier.padding(horizontal = Dimens.large)
            )

            PickerWheelColumn(
                items = secondsList,
                initialItem = customRestTime.seconds.intValue,
                formatText = { "%02d".format(it) },
                onItemSelected = {
                    Timber.i("Seconds item selected $it")
                    customRestTime.seconds.intValue = it
                }
            )
        }
    }

}

@Composable
private fun MiddleDotsSeparator(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.normal),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.dotsSeparatorSize)
                .background(MaterialTheme.colorScheme.secondary, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(Dimens.dotsSeparatorSize)
                .background(MaterialTheme.colorScheme.secondary, CircleShape)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun <T> PickerWheelColumn(
    items: List<T>,
    initialItem: T,
    formatText: (T) -> String,
    onItemSelected: (T) -> Unit,
    visibleItemsCount: Int = 3,
) {
    val initialIndex = items.indexOf(initialItem).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // Center index calculation
    val currentIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf initialIndex

            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            visibleItems.minByOrNull {
                kotlin.math.abs((it.offset + it.size / 2) - viewportCenter)
            }?.index ?: initialIndex
        }
    }

    // Emit selection when scrolling halts
    val currentOnItemSelected by rememberUpdatedState(onItemSelected)
    LaunchedEffect(listState) {
        snapshotFlow { currentIndex }
            .distinctUntilChanged()
            .collect {
                if (currentIndex in items.indices) {
                    currentOnItemSelected(items[currentIndex])
                }
            }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(Dimens.wheelWidth)
                .height(Dimens.wheelItemHeight * visibleItemsCount)
                .clip(RoundedCornerShape(Dimens.large))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .border(Dimens.thinBorder, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.large)),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(
                state = listState,
                flingBehavior = flingBehavior,
                // Vertical contentPadding centers the first and last elements
                contentPadding = PaddingValues(vertical = Dimens.wheelItemHeight * (visibleItemsCount / 2)),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(items) { index, item ->
                    val isSelected = index == currentIndex
                    val animatedAlpha by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0.45f,
                        label = "ItemAlpha"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.wheelItemHeight)
                            .padding(horizontal = Dimens.normal),
                        contentAlignment = Alignment.Center
                    ) {
                        // Highlight capsule around the selected item
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(Dimens.wheelItemHeight - Dimens.normal)
                                    .clip(RoundedCornerShape(Dimens.normal))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .border(Dimens.thinBorder, MaterialTheme.colorScheme.outline, RoundedCornerShape(Dimens.normal))
                            )
                        }

                        Text(
                            text = formatText(item),
                            modifier = Modifier.alpha(animatedAlpha)
                        )
                    }
                }
            }
        }
    }
}

private enum class TimeUnitOption(val labelRes: Int) {
    SECONDS(R.string.seconds),
    MINUTES(R.string.minutes),
    CUSTOM(R.string.custom),
}

private sealed interface RestTime {
    val unitOption: TimeUnitOption
    val duration: Duration
    val selectedValue: State<Duration?>

    sealed interface PresetValues : RestTime {
        val values: List<Int>
        fun select(idx: Int)
        val suffixStringRes: Int
        @Composable
        fun forEachIndexed(action: @Composable (Int, Int, Boolean) -> Unit)
    }

    data class Seconds(override val duration: Duration) : PresetValues {
        override val unitOption: TimeUnitOption = TimeUnitOption.SECONDS
        override val values = listOf(15, 30, 45, 60, 90, 120, 150, 180, 240)
        override val suffixStringRes: Int = R.string.seconds_suffix
        override val selectedValue: MutableState<Duration?> = mutableStateOf(
            values.firstOrNull { it.toLong() == duration.inWholeSeconds }?.seconds
        )

        override fun select(idx: Int) {
            selectedValue.value = values.getOrNull(idx)?.seconds
        }

        @Composable
        override fun forEachIndexed(action: @Composable (Int, Int, Boolean) -> Unit) {
            values.forEachIndexed { index, value ->
                val isSelected = value.seconds == selectedValue.value
                action(index, value, isSelected)
            }
        }
    }

    data class Minutes(override val duration: Duration) : PresetValues {
        override val unitOption: TimeUnitOption = TimeUnitOption.MINUTES
        override val values = listOf(1, 2, 3, 4, 5, 10)
        override val suffixStringRes: Int = R.string.minutes_suffix
        override val selectedValue: MutableState<Duration?> = mutableStateOf(
            values.firstOrNull {
                it.minutes.inWholeSeconds == duration.inWholeSeconds
            }?.minutes
        )

        override fun select(idx: Int) {
            selectedValue.value = values.getOrNull(idx)?.minutes
        }

        @Composable
        override fun forEachIndexed(action: @Composable (Int, Int, Boolean) -> Unit) {
            values.forEachIndexed { index, value ->
                val isSelected = value.minutes == selectedValue.value
                action(index, value, isSelected)
            }
        }
    }

    data class Custom(override val duration: Duration) : RestTime {
        override val unitOption: TimeUnitOption = TimeUnitOption.CUSTOM
        val minutes = mutableIntStateOf(duration.inWholeMinutes.toInt())
        val seconds = mutableIntStateOf(duration.inWholeSeconds.toInt() % 60)
        override val selectedValue: State<Duration> = derivedStateOf {
            minutes.intValue.minutes + seconds.intValue.seconds
        }
    }
}

private fun buildRestTimes(restTime: Duration): List<RestTime> {
    return listOf(
        RestTime.Seconds(restTime),
        RestTime.Minutes(restTime),
        RestTime.Custom(restTime),
    )
}

@Preview
@Composable
private fun RestTimeBottomSheetContentSecondsPreview() {
    PreviewSurface {
        RestTimeBottomSheetContent(
            onConfirmClicked = {},
            restTime = 30.seconds
        )
    }
}

@Preview
@Composable
private fun RestTimeBottomSheetContentMinutesPreview() {
    PreviewSurface {
        RestTimeBottomSheetContent(
            onConfirmClicked = {},
            restTime = 5.minutes
        )
    }
}

@Preview
@Composable
private fun RestTimeBottomSheetContentCustomPreview() {
    PreviewSurface {
        RestTimeBottomSheetContent(
            onConfirmClicked = {},
            restTime = 75.seconds
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreviewSurface(content: @Composable () -> Unit) {
    TrainingPlannerTheme {
        Surface(
            color = BottomSheetDefaults.ContainerColor,
            shape = BottomSheetDefaults.ExpandedShape,
            tonalElevation = BottomSheetDefaults.Elevation,
            content = content
        )
    }
}