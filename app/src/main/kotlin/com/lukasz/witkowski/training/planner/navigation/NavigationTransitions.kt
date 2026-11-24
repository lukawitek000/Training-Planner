package com.lukasz.witkowski.training.planner.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.navigation3.runtime.metadata
import androidx.navigation3.ui.NavDisplay
import com.lukasz.witkowski.training.planner.shared.theme.Motion

internal object NavigationTransitions {

    fun sharedAxisXForward(offsetPx: Int): ContentTransform {
        return (
            slideInHorizontally(
                animationSpec = tween(Motion.ENTER_DURATION_MS, easing = FastOutSlowInEasing),
                initialOffsetX = { offsetPx }
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = Motion.FADE_THROUGH_IN_DURATION_MS,
                    delayMillis = Motion.FADE_THROUGH_DELAY_MS,
                    easing = LinearOutSlowInEasing
                )
            )
            ) togetherWith (
            slideOutHorizontally(
                animationSpec = tween(Motion.ENTER_DURATION_MS, easing = FastOutSlowInEasing),
                targetOffsetX = { -offsetPx }
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = Motion.FADE_THROUGH_OUT_DURATION_MS,
                    easing = FastOutLinearInEasing
                )
            )
            )
    }

    fun sharedAxisXBackward(offsetPx: Int): ContentTransform {
        return (
            slideInHorizontally(
                animationSpec = tween(Motion.ENTER_DURATION_MS, easing = FastOutSlowInEasing),
                initialOffsetX = { -offsetPx }
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = Motion.FADE_THROUGH_IN_DURATION_MS,
                    delayMillis = Motion.FADE_THROUGH_DELAY_MS,
                    easing = LinearOutSlowInEasing
                )
            )
            ) togetherWith (
            slideOutHorizontally(
                animationSpec = tween(Motion.ENTER_DURATION_MS, easing = FastOutSlowInEasing),
                targetOffsetX = { offsetPx }
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = Motion.FADE_THROUGH_OUT_DURATION_MS,
                    easing = FastOutLinearInEasing
                )
            )
            )
    }

    fun fadeThrough(): ContentTransform {
        return (
            fadeIn(
                animationSpec = tween(
                    durationMillis = Motion.FADE_THROUGH_IN_DURATION_MS,
                    delayMillis = Motion.FADE_THROUGH_DELAY_MS,
                    easing = LinearOutSlowInEasing
                )
            ) + scaleIn(
                initialScale = Motion.FADE_THROUGH_INITIAL_SCALE,
                animationSpec = tween(
                    durationMillis = Motion.FADE_THROUGH_IN_DURATION_MS,
                    delayMillis = Motion.FADE_THROUGH_DELAY_MS,
                    easing = FastOutSlowInEasing
                )
            )
            ) togetherWith fadeOut(
            animationSpec = tween(
                durationMillis = Motion.FADE_THROUGH_OUT_DURATION_MS,
                easing = FastOutLinearInEasing
            )
        )
    }

    fun topLevelTabMetadata(): Map<String, Any> {
        return metadata {
            put(NavDisplay.TransitionKey) { fadeThrough() }
        }
    }

    fun verticalModalMetadata(): Map<String, Any> {
        val enter: ContentTransform = (
            slideInVertically(
                animationSpec = tween(Motion.ENTER_DURATION_MS, easing = FastOutSlowInEasing),
                initialOffsetY = { it }
            ) + fadeIn(
                animationSpec = tween(Motion.FADE_THROUGH_IN_DURATION_MS, easing = LinearOutSlowInEasing)
            )
            ) togetherWith ExitTransition.None

        val exit: ContentTransform = EnterTransition.None togetherWith (
            slideOutVertically(
                animationSpec = tween(Motion.EXIT_DURATION_MS, easing = FastOutLinearInEasing),
                targetOffsetY = { it }
            ) + fadeOut(
                animationSpec = tween(Motion.EXIT_DURATION_MS, easing = FastOutLinearInEasing)
            )
            )

        return metadata {
            put(NavDisplay.TransitionKey) { enter }
            put(NavDisplay.PopTransitionKey) { exit }
            put(NavDisplay.PredictivePopTransitionKey) { exit }
        }
    }

    fun sharedAxisYMetadata(offsetPx: Int): Map<String, Any> {
        val enter: ContentTransform = (
            slideInVertically(
                animationSpec = tween(Motion.ENTER_DURATION_MS, easing = FastOutSlowInEasing),
                initialOffsetY = { offsetPx }
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = Motion.FADE_THROUGH_IN_DURATION_MS,
                    delayMillis = Motion.FADE_THROUGH_DELAY_MS,
                    easing = LinearOutSlowInEasing
                )
            )
            ) togetherWith (
            slideOutVertically(
                animationSpec = tween(Motion.ENTER_DURATION_MS, easing = FastOutSlowInEasing),
                targetOffsetY = { -offsetPx }
            ) + fadeOut(
                animationSpec = tween(Motion.FADE_THROUGH_OUT_DURATION_MS, easing = FastOutLinearInEasing)
            )
            )

        val exit: ContentTransform = (
            slideInVertically(
                animationSpec = tween(Motion.ENTER_DURATION_MS, easing = FastOutSlowInEasing),
                initialOffsetY = { -offsetPx }
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = Motion.FADE_THROUGH_IN_DURATION_MS,
                    delayMillis = Motion.FADE_THROUGH_DELAY_MS,
                    easing = LinearOutSlowInEasing
                )
            )
            ) togetherWith (
            slideOutVertically(
                animationSpec = tween(Motion.ENTER_DURATION_MS, easing = FastOutSlowInEasing),
                targetOffsetY = { offsetPx }
            ) + fadeOut(
                animationSpec = tween(Motion.FADE_THROUGH_OUT_DURATION_MS, easing = FastOutLinearInEasing)
            )
            )

        return metadata {
            put(NavDisplay.TransitionKey) { enter }
            put(NavDisplay.PopTransitionKey) { exit }
            put(NavDisplay.PredictivePopTransitionKey) { exit }
        }
    }

    fun immersiveScaleMetadata(): Map<String, Any> {
        val enter: ContentTransform = (
            scaleIn(
                initialScale = Motion.SESSION_SCALE_INITIAL,
                animationSpec = tween(Motion.ENTER_DURATION_MS, easing = FastOutSlowInEasing)
            ) + fadeIn(
                animationSpec = tween(Motion.ENTER_DURATION_MS, easing = LinearOutSlowInEasing)
            )
            ) togetherWith fadeOut(
            animationSpec = tween(Motion.EXIT_DURATION_MS, easing = FastOutLinearInEasing)
        )

        val exit: ContentTransform = fadeIn(
            animationSpec = tween(Motion.ENTER_DURATION_MS, easing = LinearOutSlowInEasing)
        ) togetherWith (
            scaleOut(
                targetScale = Motion.SESSION_SCALE_INITIAL,
                animationSpec = tween(Motion.EXIT_DURATION_MS, easing = FastOutLinearInEasing)
            ) + fadeOut(
                animationSpec = tween(Motion.EXIT_DURATION_MS, easing = FastOutLinearInEasing)
            )
            )

        return metadata {
            put(NavDisplay.TransitionKey) { enter }
            put(NavDisplay.PopTransitionKey) { exit }
            put(NavDisplay.PredictivePopTransitionKey) { exit }
        }
    }
}
