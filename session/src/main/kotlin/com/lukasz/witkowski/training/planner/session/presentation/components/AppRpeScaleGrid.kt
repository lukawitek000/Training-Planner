package com.lukasz.witkowski.training.planner.session.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.lukasz.witkowski.training.planner.session.R
import com.lukasz.witkowski.training.planner.session.domain.models.SessionRpe
import com.lukasz.witkowski.training.planner.shared.theme.Dimens
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview
import com.lukasz.witkowski.training.planner.shared.ui.card.AppCard

/**
 * 1-10 Rate of Perceived Exertion (RPE) rating selector with dynamic descriptive feedback.
 */
@Composable
fun AppRpeScaleGrid(
    selectedRpe: SessionRpe?,
    onRpeSelected: (SessionRpe) -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spaceMd),
            verticalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.rpe_scale_title),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (selectedRpe != null) {
                    Text(
                        text = stringResource(R.string.rpe_scale_score_format, selectedRpe.score),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
            ) {
                SessionRpe.entries.take(5).forEach { rpe ->
                    RpeCell(
                        value = rpe.score,
                        isSelected = selectedRpe == rpe,
                        onClick = { onRpeSelected(rpe) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spaceSm)
            ) {
                SessionRpe.entries.drop(5).forEach { rpe ->
                    RpeCell(
                        value = rpe.score,
                        isSelected = selectedRpe == rpe,
                        onClick = { onRpeSelected(rpe) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            val descriptor = getRpeDescription(selectedRpe)
            Text(
                text = descriptor,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Dimens.spaceXs)
            )
        }
    }
}

@Composable
private fun RpeCell(
    value: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Box(
        modifier = modifier
            .size(Dimens.rpeCellSize)
            .clip(MaterialTheme.shapes.small)
            .background(backgroundColor)
            .border(
                width = if (isSelected) Dimens.mediumBorder else Dimens.thinBorder,
                color = borderColor,
                shape = MaterialTheme.shapes.small
            )
            .clickable(
                role = Role.RadioButton,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = textColor
        )
    }
}

@Composable
private fun getRpeDescription(rpe: SessionRpe?): String = when (rpe) {
    SessionRpe.VERY_LIGHT, SessionRpe.LIGHT -> stringResource(R.string.rpe_desc_warmup)
    SessionRpe.MODERATE_LIGHT, SessionRpe.MODERATE -> stringResource(R.string.rpe_desc_light)
    SessionRpe.SOMEWHAT_HARD, SessionRpe.CHALLENGING -> stringResource(
        R.string.rpe_desc_challenging
    )
    SessionRpe.HARD, SessionRpe.VERY_HARD -> stringResource(R.string.rpe_desc_hard)
    SessionRpe.NEAR_LIMIT -> stringResource(R.string.rpe_desc_near_limit)
    SessionRpe.MAXIMAL -> stringResource(R.string.rpe_desc_max_effort)
    null -> stringResource(R.string.rpe_desc_prompt)
}

@AppPreview
@Composable
private fun AppRpeScaleGridPreview() {
    TrainingPlannerTheme {
        AppRpeScaleGrid(
            selectedRpe = SessionRpe.VERY_HARD,
            onRpeSelected = {},
            modifier = Modifier.padding(Dimens.spaceMd)
        )
    }
}
