package com.lukasz.witkowski.training.planner.shared.ui.inputField

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.lukasz.witkowski.training.planner.shared.theme.DarkGrey
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview

/**
 * Text input field composable styled according to design specification using Material 3 [OutlinedTextFieldDefaults.DecorationBox].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val textStyle = MaterialTheme.typography.bodyLarge.copy(
        color = MaterialTheme.colorScheme.onSurface,
    )

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceBright,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        focusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
        unfocusedBorderColor = Color.Transparent,
        disabledBorderColor = Color.Transparent,
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedLeadingIconColor = MaterialTheme.colorScheme.secondary,
        unfocusedLeadingIconColor = MaterialTheme.colorScheme.secondary,
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens2.inputFieldHeight),
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        singleLine = singleLine,
        decorationBox = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = innerTextField,
                enabled = enabled,
                singleLine = singleLine,
                visualTransformation = visualTransformation,
                interactionSource = interactionSource,
                placeholder = if (placeholder.isNotEmpty()) {
                    {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = DarkGrey,
                        )
                    }
                } else null,
                leadingIcon = leadingIcon,
                colors = textFieldColors,
                contentPadding = PaddingValues(
                    horizontal = Dimens2.inputHorizontalPadding,
                    vertical = 0.dp,
                ),
                container = {
                    OutlinedTextFieldDefaults.Container(
                        enabled = enabled,
                        isError = false,
                        interactionSource = interactionSource,
                        colors = textFieldColors,
                        shape = MaterialTheme.shapes.medium,
                    )
                },
            )
        },
    )
}

/**
 * Convenience overload of [AppTextField] accepting an [ImageVector] as leading icon.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    leadingIconContentDescription: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val iconComposable: @Composable (() -> Unit)? = if (leadingIcon != null) {
        {
            Icon(
                imageVector = leadingIcon,
                contentDescription = leadingIconContentDescription,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(Dimens2.inputIconSize),
            )
        }
    } else null

    AppTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        leadingIcon = iconComposable,
        enabled = enabled,
        readOnly = readOnly,
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
    )
}

@AppPreview
@Composable
private fun AppTextFieldPreview() {
    TrainingPlannerTheme2 {
        Column(
            modifier = Modifier.padding(Dimens2.spaceMd),
            verticalArrangement = Arrangement.spacedBy(Dimens2.spaceMd),
        ) {
            AppTextField(
                value = "",
                onValueChange = {},
                placeholder = "athlete@ironlog.io",
                leadingIcon = Icons.Default.Email,
            )
            AppTextField(
                value = "user@ironlog.io",
                onValueChange = {},
                placeholder = "athlete@ironlog.io",
                leadingIcon = Icons.Default.Email,
            )
        }
    }
}
