package com.lukasz.witkowski.training.planner.shared.ui.button

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.lukasz.witkowski.training.planner.shared.theme.Dimens
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview

/**
 * Primary action button composable styled according to design specification.
 */
@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    AppButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        textStyle =
        MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold
        ),
        elevation = Dimens.buttonElevation,
        iconTextGap = Dimens.spaceSm
    )
}

/**
 * Convenience overload accepting [ImageVector] for leading and trailing icons.
 */
@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    leadingIconContentDescription: String? = null,
    trailingIcon: ImageVector? = null,
    trailingIconContentDescription: String? = null
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
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        textStyle =
        MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold
        ),
        elevation = Dimens.buttonElevation,
        iconTextGap = Dimens.spaceSm,
        iconSize = Dimens.inputIconSize
    )
}

@AppPreview
@Composable
private fun AppPrimaryButtonPreview() {
    TrainingPlannerTheme {
        AppPrimaryButton(
            text = "Sign In",
            onClick = {},
            modifier = Modifier.padding(Dimens.spaceMd),
            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward
        )
    }
}
