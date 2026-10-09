package com.lukasz.witkowski.training.planner.shared.ui.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview

/**
 * Common base button composable that handles leading and trailing icons, colors, text style, borders, and elevation.
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    border: BorderStroke? = null,
    elevation: Dp = Dimens2.zeroDp,
    iconTextGap: Dp = Dimens2.spaceSm,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens2.buttonHeight),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        border = border,
        elevation = if (elevation > Dimens2.zeroDp) {
            ButtonDefaults.buttonElevation(defaultElevation = elevation)
        } else {
            ButtonDefaults.buttonElevation(
                defaultElevation = Dimens2.zeroDp,
                pressedElevation = Dimens2.zeroDp,
                focusedElevation = Dimens2.zeroDp,
                hoveredElevation = Dimens2.zeroDp,
                disabledElevation = Dimens2.zeroDp,
            )
        },
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(iconTextGap))
            }
            Text(
                text = text,
                style = textStyle,
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(iconTextGap))
                trailingIcon()
            }
        }
    }
}

/**
 * Convenience overload accepting [ImageVector] for leading and trailing icons.
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    leadingIconContentDescription: String? = null,
    trailingIcon: ImageVector? = null,
    trailingIconContentDescription: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    border: BorderStroke? = null,
    elevation: Dp = Dimens2.zeroDp,
    iconTextGap: Dp = Dimens2.spaceSm,
    iconSize: Dp = Dimens2.inputIconSize,
) {
    val leadingIconComposable: @Composable (() -> Unit)? = if (leadingIcon != null) {
        {
            Icon(
                imageVector = leadingIcon,
                contentDescription = leadingIconContentDescription,
                modifier = Modifier.size(iconSize),
            )
        }
    } else null

    val trailingIconComposable: @Composable (() -> Unit)? = if (trailingIcon != null) {
        {
            Icon(
                imageVector = trailingIcon,
                contentDescription = trailingIconContentDescription,
                modifier = Modifier.size(iconSize),
            )
        }
    } else null

    AppButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIconComposable,
        trailingIcon = trailingIconComposable,
        containerColor = containerColor,
        contentColor = contentColor,
        textStyle = textStyle,
        border = border,
        elevation = elevation,
        iconTextGap = iconTextGap,
    )
}

@AppPreview
@Composable
private fun AppButtonPreview() {
    TrainingPlannerTheme2 {
        AppButton(
            text = "Button with Icons",
            onClick = {},
            modifier = Modifier.padding(Dimens2.spaceMd),
            leadingIcon = Icons.Default.Email,
            trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
            elevation = Dimens2.buttonElevation,
        )
    }
}
