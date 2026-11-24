package com.lukasz.witkowski.training.planner.shared.ui.animation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import com.lukasz.witkowski.training.planner.shared.theme.Motion

/**
 * Reusable Material 3 transition factories built from [Motion] tokens.
 */
object AppMotionTransitions {

    fun slideFadeVerticalEnter(fromTop: Boolean): EnterTransition = slideInVertically(
        animationSpec = Motion.enterTween(),
        initialOffsetY = { if (fromTop) -it else it }
    ) + fadeIn(
        animationSpec = Motion.fadeThroughInTween(withDelay = false)
    )

    fun slideFadeVerticalExit(toTop: Boolean): ExitTransition = slideOutVertically(
        animationSpec = Motion.exitTween(),
        targetOffsetY = { if (toTop) -it else it }
    ) + fadeOut(
        animationSpec = Motion.exitTween()
    )

    fun expandFadeHorizontallyEnter(): EnterTransition = fadeIn(
        animationSpec = Motion.fadeThroughInTween()
    ) + expandHorizontally(
        animationSpec = Motion.enterTween()
    )

    fun shrinkFadeHorizontallyExit(): ExitTransition = fadeOut(
        animationSpec = Motion.fadeThroughOutTween()
    ) + shrinkHorizontally(
        animationSpec = Motion.exitTween()
    )

    fun fadeThroughTransform(withScale: Boolean = true): ContentTransform {
        val enterFade = fadeIn(animationSpec = Motion.fadeThroughInTween())
        val enter = if (withScale) {
            enterFade + scaleIn(
                initialScale = Motion.FADE_THROUGH_INITIAL_SCALE,
                animationSpec = tween(
                    durationMillis = Motion.FADE_THROUGH_IN_DURATION_MS,
                    delayMillis = Motion.FADE_THROUGH_DELAY_MS,
                    easing = FastOutSlowInEasing
                )
            )
        } else {
            enterFade
        }
        return enter togetherWith fadeOut(animationSpec = Motion.fadeThroughOutTween())
    }
}
