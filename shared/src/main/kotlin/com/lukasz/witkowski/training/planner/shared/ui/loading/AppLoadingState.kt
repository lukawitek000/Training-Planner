package com.lukasz.witkowski.training.planner.shared.ui.loading

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview

/**
 * Centered loading state composable styled according to design specification.
 *
 * Spec Highlights:
 * - Central Spinner Container: 112dp x 112dp (w-28 h-28) [Dimens2.spinnerSize].
 * - Pulse Glow: Animated scaling and opacity background circle in primaryContainer color.
 * - Circular Spinner Ring: Rotating progress indicator with primaryContainer indicator and surfaceContainerHighest track.
 * - Inner Icon Box: 64dp x 64dp (w-16 h-16) [Dimens2.spinnerIconContainerSize] rounded circle surface with outlineVariant border and shadow-lg elevation.
 * - Typography: headlineSmall bold title and bodyMedium centered subtitle.
 */
@Composable
fun AppLoadingState(
    modifier: Modifier = Modifier,
    title: String = "Setting up your planner...",
    subtitle: String? = "Preparing your workout routines and local workspace.",
    icon: ImageVector = Icons.Default.FitnessCenter,
    iconContentDescription: String? = null,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AppLoadingStateInfiniteTransition")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "AppLoadingStatePulseScale",
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "AppLoadingStatePulseAlpha",
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "AppLoadingStateRotation",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Dimens2.margin),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(Dimens2.spinnerSize),
            contentAlignment = Alignment.Center,
        ) {
            // Soft Ambient Pulse Glow
            Surface(
                modifier = Modifier
                    .size(Dimens2.spinnerSize)
                    .scale(pulseScale),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = pulseAlpha),
            ) {}

            // Spinner Ring
            CircularProgressIndicator(
                modifier = Modifier
                    .size(Dimens2.spinnerSize)
                    .rotate(rotation),
                color = MaterialTheme.colorScheme.primaryContainer,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                strokeWidth = Dimens2.spinnerStrokeWidth,
                strokeCap = StrokeCap.Round,
            )

            // Centered Brand Dumbbell Icon Container
            Surface(
                modifier = Modifier.size(Dimens2.spinnerIconContainerSize),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(
                    width = Dimens2.thinBorder,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                ),
                shadowElevation = Dimens2.shadowLg,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = iconContentDescription,
                        tint = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(Dimens2.iconSize24),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens2.spaceLg))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .widthIn(max = Dimens2.maxSubtitleWidth)
                    .padding(top = Dimens2.spaceXs),
            )
        }
    }
}

@AppPreview
@Composable
private fun AppLoadingStatePreview() {
    TrainingPlannerTheme2 {
        AppLoadingState()
    }
}
