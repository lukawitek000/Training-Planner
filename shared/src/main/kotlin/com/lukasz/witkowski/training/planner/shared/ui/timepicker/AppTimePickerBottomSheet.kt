package com.lukasz.witkowski.training.planner.shared.ui.timepicker

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.lukasz.witkowski.training.planner.shared.R
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.badges.AppMetadataPill
import com.lukasz.witkowski.training.planner.shared.ui.button.AppPrimaryButton
import com.lukasz.witkowski.training.planner.shared.ui.segmentedControl.AppSegmentedControl
import com.lukasz.witkowski.training.planner.shared.ui.segmentedControl.AppSegmentedControlItem
import kotlin.math.abs
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Mode tabs inside [AppTimePickerBottomSheet].
 */
enum class AppTimePickerTab {
    SECONDS,
    MINUTES,
    CUSTOM
}

private val DEFAULT_SECONDS_PRESETS = listOf(15, 30, 45, 60, 90, 120, 150, 180, 240)
private val DEFAULT_MINUTES_PRESETS = listOf(1, 2, 3, 4, 5, 10)
private const val SECONDS_PER_MINUTE = 60
private const val MILLIS_PER_SECOND = 1000L

/**
 * Stitch Universal Time Picker modal bottom sheet (`2b3ca74373e24dcfa028180f175696b0`) accepting [Time].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTimePickerBottomSheet(
    initialTime: Time?,
    onTimeConfirmed: (Time?) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.time_picker_select_time),
    subtitle: String? = stringResource(R.string.time_picker_subtitle),
    contextBadgeText: String? = null,
    allowNoTime: Boolean = false,
    secondsPresets: List<Int> = DEFAULT_SECONDS_PRESETS,
    minutesPresets: List<Int> = DEFAULT_MINUTES_PRESETS
) {
    val initialSeconds = initialTime?.let { (it.timeInMillis / MILLIS_PER_SECOND).toInt() }
    AppTimePickerBottomSheet(
        initialSeconds = initialSeconds,
        onSecondsConfirmed = { seconds ->
            onTimeConfirmed(seconds?.let { Time(seconds = it) })
        },
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        contextBadgeText = contextBadgeText,
        allowNoTime = allowNoTime,
        secondsPresets = secondsPresets,
        minutesPresets = minutesPresets
    )
}

/**
 * Stitch Universal Time Picker modal bottom sheet (`2b3ca74373e24dcfa028180f175696b0`) accepting seconds [Int].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTimePickerBottomSheet(
    initialSeconds: Int?,
    onSecondsConfirmed: (Int?) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.time_picker_select_time),
    subtitle: String? = stringResource(R.string.time_picker_subtitle),
    contextBadgeText: String? = null,
    allowNoTime: Boolean = false,
    secondsPresets: List<Int> = DEFAULT_SECONDS_PRESETS,
    minutesPresets: List<Int> = DEFAULT_MINUTES_PRESETS
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        shape =
        RoundedCornerShape(
            topStart = Dimens2.bottomSheetCornerRadius,
            topEnd = Dimens2.bottomSheetCornerRadius
        ),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = {
            Box(
                modifier =
                Modifier
                    .padding(
                        top = Dimens2.spaceGapIconTextMedium,
                        bottom = Dimens2.spaceSm
                    )
                    .size(
                        width = Dimens2.bottomSheetDragHandleWidth,
                        height = Dimens2.bottomSheetDragHandleHeight
                    )
                    .background(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                        shape = CircleShape
                    )
            )
        }
    ) {
        AppTimePickerSheetContent(
            initialSeconds = initialSeconds,
            onSecondsConfirmed = onSecondsConfirmed,
            onDismissRequest = onDismissRequest,
            title = title,
            subtitle = subtitle,
            contextBadgeText = contextBadgeText,
            allowNoTime = allowNoTime,
            secondsPresets = secondsPresets,
            minutesPresets = minutesPresets
        )
    }
}

/**
 * Stateless/Previewable content of the Stitch Universal Time Picker bottom sheet.
 */
@Composable
fun AppTimePickerSheetContent(
    initialSeconds: Int?,
    onSecondsConfirmed: (Int?) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.time_picker_select_time),
    subtitle: String? = stringResource(R.string.time_picker_subtitle),
    contextBadgeText: String? = null,
    allowNoTime: Boolean = false,
    secondsPresets: List<Int> = DEFAULT_SECONDS_PRESETS,
    minutesPresets: List<Int> = DEFAULT_MINUTES_PRESETS
) {
    val resolvedInitialTab =
        remember(initialSeconds, secondsPresets, minutesPresets) {
            when {
                initialSeconds == null || initialSeconds in secondsPresets -> AppTimePickerTab.SECONDS
                initialSeconds % SECONDS_PER_MINUTE == 0 &&
                    (initialSeconds / SECONDS_PER_MINUTE) in minutesPresets -> AppTimePickerTab.MINUTES
                else -> AppTimePickerTab.CUSTOM
            }
        }

    var selectedTab by rememberSaveable(initialSeconds) { mutableStateOf(resolvedInitialTab) }
    var selectedSeconds by rememberSaveable(initialSeconds) {
        mutableStateOf(
            if (initialSeconds == null && !allowNoTime) {
                secondsPresets.firstOrNull() ?: 45
            } else {
                initialSeconds
            }
        )
    }

    var customMinutes by rememberSaveable(initialSeconds) {
        mutableIntStateOf((selectedSeconds ?: 90) / SECONDS_PER_MINUTE)
    }
    var customRemainderSeconds by rememberSaveable(initialSeconds) {
        val rawRem = (selectedSeconds ?: 90) % SECONDS_PER_MINUTE
        mutableIntStateOf(rawRem)
    }

    val secondsTabLabel = stringResource(R.string.time_picker_tab_seconds)
    val minutesTabLabel = stringResource(R.string.time_picker_tab_minutes)
    val customTabLabel = stringResource(R.string.time_picker_tab_custom)

    val segmentedItems =
        remember(secondsTabLabel, minutesTabLabel, customTabLabel) {
            listOf(
                AppSegmentedControlItem(value = AppTimePickerTab.SECONDS, label = secondsTabLabel),
                AppSegmentedControlItem(value = AppTimePickerTab.MINUTES, label = minutesTabLabel),
                AppSegmentedControlItem(value = AppTimePickerTab.CUSTOM, label = customTabLabel)
            )
        }

    Column(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(
                start = Dimens2.spaceMd,
                end = Dimens2.spaceMd,
                bottom = Dimens2.spaceLg
            )
            .testTag("TimePickerBottomSheet")
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceSm)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!contextBadgeText.isNullOrBlank()) {
                        AppMetadataPill(
                            text = contextBadgeText,
                            leadingIcon = Icons.Default.Timer,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(Dimens2.spaceXs))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (allowNoTime) {
                val isNoneSelected = selectedSeconds == null
                Spacer(modifier = Modifier.width(Dimens2.spaceSm))
                Surface(
                    onClick = { selectedSeconds = null },
                    shape = CircleShape,
                    color =
                    if (isNoneSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                    contentColor =
                    if (isNoneSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    border =
                    BorderStroke(
                        width = Dimens2.thinBorder,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    ),
                    shadowElevation = Dimens2.shadowSm,
                    modifier = Modifier.testTag("TimePickerNoTimeButton")
                ) {
                    Row(
                        modifier =
                        Modifier.padding(
                            horizontal = Dimens2.inputHorizontalPadding,
                            vertical = Dimens2.spaceGapLabelInput
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceXs)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            tint =
                            if (isNoneSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                            modifier = Modifier.size(Dimens2.iconSize16)
                        )
                        Text(
                            text = stringResource(R.string.time_picker_no_time),
                            style =
                            MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens2.spaceMd))

        // Segmented Control (Seconds / Minutes / Custom)
        AppSegmentedControl(
            items = segmentedItems,
            selectedItem = selectedTab,
            onItemSelected = { newTab ->
                selectedTab = newTab
                if (newTab == AppTimePickerTab.CUSTOM && selectedSeconds != null) {
                    customMinutes = (selectedSeconds ?: 0) / SECONDS_PER_MINUTE
                    customRemainderSeconds = (selectedSeconds ?: 0) % SECONDS_PER_MINUTE
                } else if (newTab == AppTimePickerTab.CUSTOM && selectedSeconds == null) {
                    selectedSeconds =
                        customMinutes * SECONDS_PER_MINUTE + customRemainderSeconds
                }
            }
        )

        Spacer(modifier = Modifier.height(Dimens2.spaceMd))

        // Mode Views
        when (selectedTab) {
            AppTimePickerTab.SECONDS -> {
                TimePresetGrid(
                    presets = secondsPresets,
                    selectedValue = selectedSeconds,
                    formatLabel = { value ->
                        stringResource(R.string.time_picker_seconds_preset, value)
                    },
                    onPresetClick = { value ->
                        selectedSeconds = value
                    }
                )
            }

            AppTimePickerTab.MINUTES -> {
                val selectedMinutePreset =
                    selectedSeconds?.takeIf { it % SECONDS_PER_MINUTE == 0 }?.div(SECONDS_PER_MINUTE)
                TimePresetGrid(
                    presets = minutesPresets,
                    selectedValue = selectedMinutePreset,
                    formatLabel = { value ->
                        stringResource(R.string.time_picker_minutes_preset, value)
                    },
                    onPresetClick = { minutes ->
                        selectedSeconds = minutes * SECONDS_PER_MINUTE
                    }
                )
            }

            AppTimePickerTab.CUSTOM -> {
                CustomTimeWheelSelector(
                    minutes = customMinutes,
                    seconds = customRemainderSeconds,
                    onMinutesChanged = { newMinutes ->
                        customMinutes = newMinutes
                        selectedSeconds = newMinutes * SECONDS_PER_MINUTE + customRemainderSeconds
                    },
                    onSecondsChanged = { newSeconds ->
                        customRemainderSeconds = newSeconds
                        selectedSeconds = customMinutes * SECONDS_PER_MINUTE + newSeconds
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens2.spaceLg))

        // Primary Confirm Button
        val formattedSelection = formatDurationLabel(selectedSeconds)
        val confirmButtonLabel =
            if (selectedSeconds == null) {
                stringResource(R.string.time_picker_confirm_no_timer)
            } else {
                stringResource(R.string.time_picker_set_time, formattedSelection)
            }

        AppPrimaryButton(
            text = confirmButtonLabel,
            onClick = { onSecondsConfirmed(selectedSeconds) },
            leadingIcon = Icons.Default.CheckCircle,
            enabled = allowNoTime || selectedSeconds != null,
            modifier = Modifier.testTag("TimePickerConfirmButton")
        )

        Spacer(modifier = Modifier.height(Dimens2.spaceXs))

        TextButton(
            onClick = onDismissRequest,
            modifier =
            Modifier
                .fillMaxWidth()
                .testTag("TimePickerCancelButton")
        ) {
            Text(
                text = stringResource(R.string.cancel),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TimePresetGrid(
    presets: List<Int>,
    selectedValue: Int?,
    formatLabel: @Composable (Int) -> String,
    onPresetClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = remember(presets) { presets.chunked(3) }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens2.inputIconSpacing)
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens2.inputIconSpacing)
            ) {
                rowItems.forEach { preset ->
                    val isSelected = preset == selectedValue
                    val label = formatLabel(preset)
                    Surface(
                        onClick = { onPresetClick(preset) },
                        modifier =
                        Modifier
                            .weight(1f)
                            .height(Dimens2.buttonHeight)
                            .testTag("TimePreset_$preset"),
                        shape = MaterialTheme.shapes.medium,
                        color =
                        if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        },
                        contentColor =
                        if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        shadowElevation = if (isSelected) Dimens2.shadowSm else Dimens2.zeroDp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = label,
                                style =
                                MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    }
                }
                val emptySlots = 3 - rowItems.size
                repeat(emptySlots) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CustomTimeWheelSelector(
    minutes: Int,
    seconds: Int,
    onMinutesChanged: (Int) -> Unit,
    onSecondsChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val minutesOptions = remember { (0..60).toList() }
    val secondsOptions = remember { (0..59).toList() }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f)
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Dimens2.spaceLg,
                    vertical = Dimens2.spaceGapIconTextMedium
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceXl)
            ) {
                Text(
                    text = stringResource(R.string.time_picker_tab_minutes),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.time_picker_tab_seconds),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(Dimens2.spaceSm))

            Box(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .height(Dimens2.wheelPickerHeight),
                contentAlignment = Alignment.Center
            ) {
                // Center Selection Highlight Bar
                Surface(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(Dimens2.wheelPickerRowHeight),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {}

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens2.spaceXl),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WheelColumnPicker(
                        items = minutesOptions,
                        selectedItem = minutes,
                        formatItem = { it.toString() },
                        onItemSelected = onMinutesChanged,
                        modifier = Modifier.weight(1f)
                    )
                    WheelColumnPicker(
                        items = secondsOptions,
                        selectedItem = seconds,
                        formatItem = { "%02d".format(it) },
                        onItemSelected = onSecondsChanged,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens2.spaceSm))

            Text(
                text = stringResource(R.string.time_picker_custom_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelColumnPicker(
    items: List<Int>,
    selectedItem: Int,
    formatItem: (Int) -> String,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialIndex = remember(items, selectedItem) { items.indexOf(selectedItem).coerceAtLeast(0) }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val coroutineScope = rememberCoroutineScope()

    val currentIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) return@derivedStateOf initialIndex
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            visibleItems.minByOrNull { itemInfo ->
                abs((itemInfo.offset + itemInfo.size / 2) - viewportCenter)
            }?.index ?: initialIndex
        }
    }

    val currentOnItemSelected by rememberUpdatedState(onItemSelected)
    LaunchedEffect(listState) {
        snapshotFlow { currentIndex }
            .distinctUntilChanged()
            .collect { index ->
                if (index in items.indices) {
                    currentOnItemSelected(items[index])
                }
            }
    }

    val verticalPadding = (Dimens2.wheelPickerHeight - Dimens2.wheelPickerRowHeight) / 2

    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        contentPadding = PaddingValues(vertical = verticalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
        modifier
            .height(Dimens2.wheelPickerHeight)
    ) {
        itemsIndexed(items) { index, item ->
            val isSelected = index == currentIndex
            val animatedAlpha by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.45f,
                label = "WheelItemAlpha"
            )

            Box(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .height(Dimens2.wheelPickerRowHeight)
                    .clickable {
                        onItemSelected(item)
                        coroutineScope.launch {
                            listState.animateScrollToItem(index)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formatItem(item),
                    style =
                    if (isSelected) {
                        MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    } else {
                        MaterialTheme.typography.titleSmall
                    },
                    color =
                    if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.alpha(animatedAlpha)
                )
            }
        }
    }
}

@Composable
private fun formatDurationLabel(seconds: Int?): String {
    if (seconds == null) return ""
    val mins = seconds / SECONDS_PER_MINUTE
    val remSecs = seconds % SECONDS_PER_MINUTE
    return when {
        mins > 0 && remSecs > 0 ->
            "$mins ${stringResource(R.string.minutes_shortcut)} $remSecs ${stringResource(R.string.seconds_shortcut)}"
        mins > 0 && seconds !in DEFAULT_SECONDS_PRESETS ->
            stringResource(R.string.time_picker_minutes_preset, mins)
        else ->
            stringResource(R.string.time_picker_seconds_preset, seconds)
    }
}

@AppPreview
@Composable
private fun AppTimePickerSecondsPreview() {
    TrainingPlannerTheme2 {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape =
            RoundedCornerShape(
                topStart = Dimens2.bottomSheetCornerRadius,
                topEnd = Dimens2.bottomSheetCornerRadius
            )
        ) {
            AppTimePickerSheetContent(
                initialSeconds = 45,
                onSecondsConfirmed = {},
                onDismissRequest = {},
                contextBadgeText = "Hold Time",
                allowNoTime = true
            )
        }
    }
}

@AppPreview
@Composable
private fun AppTimePickerCustomPreview() {
    TrainingPlannerTheme2 {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape =
            RoundedCornerShape(
                topStart = Dimens2.bottomSheetCornerRadius,
                topEnd = Dimens2.bottomSheetCornerRadius
            )
        ) {
            AppTimePickerSheetContent(
                initialSeconds = 95,
                onSecondsConfirmed = {},
                onDismissRequest = {},
                contextBadgeText = "Rest Time",
                allowNoTime = false
            )
        }
    }
}
