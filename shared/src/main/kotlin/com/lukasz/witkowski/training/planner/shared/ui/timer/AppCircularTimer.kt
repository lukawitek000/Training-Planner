package com.lukasz.witkowski.training.planner.shared.ui.timer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview

/**
 * Animated circular countdown ring for rest intervals and active timers.
 */
@Composable
fun AppCircularTimer(
    timeFormatted: String,
    progress: Float,
    modifier: Modifier = Modifier,
    label: String? = null,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    progressColor: Color = MaterialTheme.colorScheme.primary
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "CircularTimerProgress"
    )

    Box(
        modifier = modifier.size(Dimens2.timerRingSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(Dimens2.timerRingSize)
                .padding(Dimens2.spaceSm)
        ) {
            val strokeWidthPx = Dimens2.timerRingStrokeWidth.toPx()

            // Background circle track
            drawCircle(
                color = trackColor,
                style = Stroke(width = strokeWidthPx)
            )

            // Active progress arc
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(
                    width = strokeWidthPx,
                    cap = StrokeCap.Round
                )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (label != null) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@AppPreview
@Composable
private fun AppCircularTimerPreview() {
    TrainingPlannerTheme2 {
        AppCircularTimer(
            timeFormatted = "01:15",
            progress = 0.65f,
            label = "Rest Interval",
            modifier = Modifier.padding(Dimens2.spaceLg)
        )
    }
}
