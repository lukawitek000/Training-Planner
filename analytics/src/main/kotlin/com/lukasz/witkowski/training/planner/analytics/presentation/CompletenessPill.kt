package com.lukasz.witkowski.training.planner.analytics.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import com.lukasz.witkowski.training.planner.analytics.R
import com.lukasz.witkowski.training.planner.shared.theme.Dimens
import com.lukasz.witkowski.training.planner.shared.theme.ErrorColor
import com.lukasz.witkowski.training.planner.shared.theme.ShapeFull
import com.lukasz.witkowski.training.planner.shared.theme.Success
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.theme.Warning
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.utils.formatUpToTwoDecimals

@Composable
internal fun CompletenessPill(
    completenessRate: Double,
    modifier: Modifier = Modifier,
    includeLabel: Boolean = true
) {
    val completenessStyle = remember(completenessRate) {
        CompletenessStyle.create(completenessRate)
    }
    val textStyle = MaterialTheme.typography.labelMedium
    Row(
        modifier = modifier
            .background(
                color = completenessStyle.backgroundColor,
                shape = ShapeFull
            )
            .padding(horizontal = Dimens.spaceSm, vertical = Dimens.spaceXs),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = completenessStyle.icon,
            contentDescription = null,
            tint = completenessStyle.contentColor,
            modifier = Modifier.size(Dimens.iconSize16)
        )
        CompletenessPercentage(
            completenessRate = completenessRate,
            completenessStyle = completenessStyle,
            textStyle = textStyle
        )
        if (includeLabel) {
            Text(
                text = stringResource(R.string.completed),
                color = completenessStyle.contentColor,
                style = textStyle
            )
        }
    }
}

@Composable
internal fun CompletenessPercentage(
    completenessRate: Double,
    completenessStyle: CompletenessStyle,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium
) {
    Text(
        text = stringResource(
            R.string.completeness,
            completenessRate.formatUpToTwoDecimals()
        ),
        color = completenessStyle.contentColor,
        style = textStyle
    )
}

internal sealed class CompletenessStyle {
    abstract val contentColor: Color
    abstract val backgroundColor: Color
    abstract val icon: ImageVector

    data object Completed : CompletenessStyle() {
        override val contentColor: Color = Success
        override val backgroundColor: Color = contentColor.copy(alpha = 0.2f)
        override val icon: ImageVector = Icons.Default.CheckCircle
    }

    data object PartialCompleteness : CompletenessStyle() {
        override val contentColor: Color = Warning
        override val backgroundColor: Color = contentColor.copy(alpha = 0.15f)
        override val icon: ImageVector = Icons.Default.Timelapse
    }

    data object CriticalCompleteness : CompletenessStyle() {
        override val contentColor: Color = ErrorColor
        override val backgroundColor: Color = contentColor.copy(alpha = 0.2f)
        override val icon: ImageVector = Icons.Default.LocalFireDepartment
    }

    companion object {
        fun create(completenessRate: Double): CompletenessStyle = when {
            completenessRate < 20.0 -> CriticalCompleteness
            completenessRate == 100.0 -> Completed
            else -> PartialCompleteness
        }
    }
}

@AppPreview
@Composable
private fun CompletenessPillPreview() {
    TrainingPlannerTheme {
        val completenessRates = listOf(12.0, 20.51, 50.0, 75.2, 100.0)
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.spaceMd)) {
            completenessRates.forEach {
                CompletenessPill(it)
            }
        }
    }
}
