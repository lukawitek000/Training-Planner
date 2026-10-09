package com.lukasz.witkowski.training.planner.shared.ui.inputField

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2

/**
 * Combined label and text input field composable styled according to design specification.
 */
@Composable
fun AppLabeledTextField(
    label: String,
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
    Column(modifier = modifier) {
        AppInputLabel(text = label)
        Spacer(modifier = Modifier.height(Dimens2.spaceGapLabelInput))
        AppTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
        )
    }
}

/**
 * Convenience overload of [AppLabeledTextField] accepting an [ImageVector] as leading icon.
 */
@Composable
fun AppLabeledTextField(
    label: String,
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
    Column(modifier = modifier) {
        AppInputLabel(text = label)
        Spacer(modifier = Modifier.height(Dimens2.spaceGapLabelInput))
        AppTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            leadingIconContentDescription = leadingIconContentDescription,
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A120A)
@Composable
private fun AppLabeledTextFieldPreview() {
    TrainingPlannerTheme2 {
        Column(
            modifier = Modifier.padding(Dimens2.spaceMd),
            verticalArrangement = Arrangement.spacedBy(Dimens2.spaceMd),
        ) {
            AppLabeledTextField(
                label = "Email Address",
                value = "",
                onValueChange = {},
                placeholder = "athlete@ironlog.io",
                leadingIcon = Icons.Default.Email,
            )
            AppLabeledTextField(
                label = "Email Address",
                value = "user@ironlog.io",
                onValueChange = {},
                placeholder = "athlete@ironlog.io",
                leadingIcon = Icons.Default.Email,
            )
        }
    }
}
