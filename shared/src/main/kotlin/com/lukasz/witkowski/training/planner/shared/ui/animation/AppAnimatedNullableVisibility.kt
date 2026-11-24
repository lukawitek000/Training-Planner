package com.lukasz.witkowski.training.planner.shared.ui.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.lukasz.witkowski.training.planner.shared.theme.Motion

/**
 * Animates visibility of [value] while retaining the last non-null value during the exit transition
 * so [content] can animate out smoothly instead of disappearing on the first frame.
 */
@Composable
fun <T : Any> AppAnimatedNullableVisibility(
    value: T?,
    modifier: Modifier = Modifier,
    enter: EnterTransition = fadeIn(Motion.enterTween()),
    exit: ExitTransition = fadeOut(Motion.exitTween()),
    label: String = "AppAnimatedNullableVisibility",
    content: @Composable AnimatedVisibilityScope.(T) -> Unit
) {
    var lastNonNullValue by remember { mutableStateOf(value) }
    if (value != null) {
        lastNonNullValue = value
    }
    AnimatedVisibility(
        visible = value != null,
        modifier = modifier,
        enter = enter,
        exit = exit,
        label = label
    ) {
        lastNonNullValue?.let { content(it) }
    }
}
