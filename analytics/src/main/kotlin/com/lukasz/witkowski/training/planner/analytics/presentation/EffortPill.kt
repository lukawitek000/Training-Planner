package com.lukasz.witkowski.training.planner.analytics.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.lukasz.witkowski.training.planner.analytics.R
import com.lukasz.witkowski.training.planner.session.domain.models.SessionRpe
import com.lukasz.witkowski.training.planner.shared.theme.Dimens
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EffortPill(
    rpe: SessionRpe,
    modifier: Modifier = Modifier,
) {
    val style = rpe.toVisualStyle()
    val label = stringResource(rpe.labelRes)
    val description = stringResource(rpe.descriptionRes)
    val text = stringResource(R.string.effort_long_summary, label, rpe.score)
    val tooltipState = rememberTooltipState(isPersistent = true)
    val scope = rememberCoroutineScope()

    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            TooltipAnchorPosition.Below,
        ),
        tooltip = {
            PlainTooltip() { Text(description) }
        },
        state = tooltipState
    ) {
        Surface(
            modifier = modifier.clickable {
                scope.launch {
                    tooltipState.show()
                }
            },
            shape = MaterialTheme.shapes.small,
            color = style.containerColor,
            contentColor = style.contentColor
        ) {
            Row(
                modifier = Modifier.padding(all = Dimens.spaceXs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
            ) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = null,
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private data class RpeVisualStyle(
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color
)

@Composable
private fun SessionRpe.toVisualStyle(
    darkTheme: Boolean = isSystemInDarkTheme()
): RpeVisualStyle {
    val icon = when (this) {
        SessionRpe.VERY_LIGHT -> Icons.Rounded.Spa
        SessionRpe.LIGHT -> Icons.Rounded.Air
        SessionRpe.MODERATE_LIGHT -> Icons.AutoMirrored.Rounded.DirectionsWalk
        SessionRpe.MODERATE -> Icons.AutoMirrored.Rounded.DirectionsRun
        SessionRpe.SOMEWHAT_HARD -> Icons.Rounded.Speed
        SessionRpe.CHALLENGING -> Icons.AutoMirrored.Rounded.TrendingUp
        SessionRpe.HARD -> Icons.Rounded.FitnessCenter
        SessionRpe.VERY_HARD -> Icons.Rounded.Bolt
        SessionRpe.NEAR_LIMIT -> Icons.Rounded.LocalFireDepartment
        SessionRpe.MAXIMAL -> Icons.Rounded.Whatshot
    }

    val (container, content) = when (this) {
        SessionRpe.VERY_LIGHT, SessionRpe.LIGHT -> if (darkTheme) {
            Color(0xFF0C4A6E) to Color(0xFFBAE6FD)
        } else {
            Color(0xFFE0F2FE) to Color(0xFF0369A1)
        }

        SessionRpe.MODERATE_LIGHT, SessionRpe.MODERATE -> if (darkTheme) {
            Color(0xFF14532D) to Color(0xFFBBF7D0)
        } else {
            Color(0xFFDCFCE7) to Color(0xFF15803D)
        }

        SessionRpe.SOMEWHAT_HARD, SessionRpe.CHALLENGING -> if (darkTheme) {
            Color(0xFF78350F) to Color(0xFFFDE68A)
        } else {
            Color(0xFFFEF3C7) to Color(0xFFB45309)
        }

        SessionRpe.HARD, SessionRpe.VERY_HARD -> if (darkTheme) {
            Color(0xFF7C2D12) to Color(0xFFFED7AA)
        } else {
            Color(0xFFFFEDD5) to Color(0xFFC2410C)
        }

        SessionRpe.NEAR_LIMIT, SessionRpe.MAXIMAL -> if (darkTheme) {
            Color(0xFF7F1D1D) to Color(0xFFFECACA)
        } else {
            Color(0xFFFEE2E2) to Color(0xFFB91C1C)
        }
    }

    return RpeVisualStyle(
        icon = icon,
        containerColor = container,
        contentColor = content
    )
}

@get:StringRes
val SessionRpe.labelRes: Int
    get() = when (this) {
        SessionRpe.VERY_LIGHT -> R.string.rpe_very_light
        SessionRpe.LIGHT -> R.string.rpe_light
        SessionRpe.MODERATE_LIGHT -> R.string.rpe_moderate_light
        SessionRpe.MODERATE -> R.string.rpe_moderate
        SessionRpe.SOMEWHAT_HARD -> R.string.rpe_somewhat_hard
        SessionRpe.CHALLENGING -> R.string.rpe_challenging
        SessionRpe.HARD -> R.string.rpe_hard
        SessionRpe.VERY_HARD -> R.string.rpe_very_hard
        SessionRpe.NEAR_LIMIT -> R.string.rpe_near_limit
        SessionRpe.MAXIMAL -> R.string.rpe_maximal
    }

@get:StringRes
val SessionRpe.descriptionRes: Int
    get() = when (this) {
        SessionRpe.VERY_LIGHT -> R.string.rpe_very_light_desc
        SessionRpe.LIGHT -> R.string.rpe_light_desc
        SessionRpe.MODERATE_LIGHT -> R.string.rpe_moderate_light_desc
        SessionRpe.MODERATE -> R.string.rpe_moderate_desc
        SessionRpe.SOMEWHAT_HARD -> R.string.rpe_somewhat_hard_desc
        SessionRpe.CHALLENGING -> R.string.rpe_challenging_desc
        SessionRpe.HARD -> R.string.rpe_hard_desc
        SessionRpe.VERY_HARD -> R.string.rpe_very_hard_desc
        SessionRpe.NEAR_LIMIT -> R.string.rpe_near_limit_desc
        SessionRpe.MAXIMAL -> R.string.rpe_maximal_desc
    }

@AppPreview
@Composable
private fun EffortPillPreview() {
    TrainingPlannerTheme {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceMd)) {
            SessionRpe.entries.forEach {
                EffortPill(it)
            }
        }
    }
}