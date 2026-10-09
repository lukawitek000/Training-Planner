package com.lukasz.witkowski.training.planner.shared.ui.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2

/**
 * Secondary surface action button composable styled according to design specification.
 */
@Composable
fun AppSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    AppButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        textStyle = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Medium,
        ),
        border = BorderStroke(
            width = Dimens2.thinBorder,
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.4f),
        ),
        iconTextGap = Dimens2.spaceGapIconTextMedium,
    )
}

/**
 * Convenience overload accepting [ImageVector] for leading and trailing icons.
 */
@Composable
fun AppSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    leadingIconContentDescription: String? = null,
    trailingIcon: ImageVector? = null,
    trailingIconContentDescription: String? = null,
) {
    AppButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon,
        leadingIconContentDescription = leadingIconContentDescription,
        trailingIcon = trailingIcon,
        trailingIconContentDescription = trailingIconContentDescription,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        textStyle = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Medium,
        ),
        border = BorderStroke(
            width = Dimens2.thinBorder,
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.4f),
        ),
        iconTextGap = Dimens2.spaceGapIconTextMedium,
        iconSize = Dimens2.iconSize16,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1A120A)
@Composable
private fun AppSecondaryButtonPreview() {
    TrainingPlannerTheme2 {
        AppSecondaryButton(
            text = "Continue with Google",
            onClick = {},
            modifier = Modifier.padding(Dimens2.spaceMd),
        )
    }
}
