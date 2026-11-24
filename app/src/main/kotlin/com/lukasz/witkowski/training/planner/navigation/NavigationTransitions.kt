package com.lukasz.witkowski.training.planner.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
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
import com.lukasz.witkowski.training.planner.shared.ui.animation.AppMotionTransitions

internal object NavigationTransitions {

    fun sharedAxisXForward(offsetPx: Int): ContentTransform = sharedAxisX(offsetPx = offsetPx)

    fun sharedAxisXBackward(offsetPx: Int): ContentTransform = sharedAxisX(offsetPx = -offsetPx)

    fun topLevelTabMetadata(): Map<String, Any> {
        return metadata {
            put(NavDisplay.TransitionKey) { AppMotionTransitions.fadeThroughTransform() }
        }
    }

    fun verticalModalMetadata(): Map<String, Any> {
        val enter: ContentTransform =
            AppMotionTransitions.slideFadeVerticalEnter(fromTop = false) togetherWith
                fadeOut(Motion.exitTween())

        val exit: ContentTransform =
            EnterTransition.None togetherWith
                AppMotionTransitions.slideFadeVerticalExit(toTop = false)

        return modalMetadata(enter = enter, exit = exit)
    }

    fun sharedAxisYMetadata(offsetPx: Int): Map<String, Any> {
        return modalMetadata(
            enter = sharedAxisY(offsetPx = offsetPx),
            exit = sharedAxisY(offsetPx = -offsetPx),
        )
    }

    fun immersiveScaleMetadata(): Map<String, Any> {
        val enterFade = fadeIn(
            animationSpec = tween(Motion.ENTER_DURATION_MS, easing = LinearOutSlowInEasing)
        )
        val enter: ContentTransform = (
            scaleIn(
                initialScale = Motion.SESSION_SCALE_INITIAL,
                animationSpec = Motion.enterTween(),
            ) + enterFade
            ) togetherWith fadeOut(Motion.exitTween())

        val exit: ContentTransform = enterFade togetherWith (
            scaleOut(
                targetScale = Motion.SESSION_SCALE_INITIAL,
                animationSpec = Motion.exitTween(),
            ) + fadeOut(Motion.exitTween())
            )

        return modalMetadata(enter = enter, exit = exit)
    }

    private fun sharedAxisX(offsetPx: Int): ContentTransform {
        return (
            slideInHorizontally(
                animationSpec = Motion.enterTween(),
                initialOffsetX = { offsetPx },
            ) + fadeIn(animationSpec = Motion.fadeThroughInTween())
            ) togetherWith (
            slideOutHorizontally(
                animationSpec = Motion.enterTween(),
                targetOffsetX = { -offsetPx },
            ) + fadeOut(animationSpec = Motion.fadeThroughOutTween())
            )
    }

    private fun sharedAxisY(offsetPx: Int): ContentTransform {
        return (
            slideInVertically(
                animationSpec = Motion.enterTween(),
                initialOffsetY = { offsetPx },
            ) + fadeIn(animationSpec = Motion.fadeThroughInTween())
            ) togetherWith (
            slideOutVertically(
                animationSpec = Motion.enterTween(),
                targetOffsetY = { -offsetPx },
            ) + fadeOut(animationSpec = Motion.fadeThroughOutTween())
            )
    }

    private fun modalMetadata(
        enter: ContentTransform,
        exit: ContentTransform,
    ): Map<String, Any> {
        return metadata {
            put(NavDisplay.TransitionKey) { enter }
            put(NavDisplay.PopTransitionKey) { exit }
            put(NavDisplay.PredictivePopTransitionKey) { exit }
        }
    }
}
