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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import com.lukasz.witkowski.training.planner.shared.R
import com.lukasz.witkowski.training.planner.shared.theme.Dimens2
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerTheme2
import com.lukasz.witkowski.training.planner.shared.ui.AppPreview

/**
 * Rounded search input bar composable styled according to Stitch design specification.
 *
 * Spec Highlights:
 * - Shape: [MaterialTheme.shapes.large] (16dp)
 * - Unfocused container: [MaterialTheme.colorScheme.surfaceContainerHigh]
 * - Focused container: [MaterialTheme.colorScheme.surfaceContainerHighest]
 * - Leading Icon: [Icons.Default.Search] tinted [MaterialTheme.colorScheme.primaryContainer]
 * - Trailing Clear Button: [IconButton] shown when `query.isNotEmpty()`
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    onClearClick: () -> Unit = { onQueryChange("") },
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val focusManager = LocalFocusManager.current
    val textStyle =
        MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface
        )

    val textFieldColors =
        OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedBorderColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primaryContainer,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.primaryContainer,
            focusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier =
        modifier
            .fillMaxWidth()
            .height(Dimens2.inputFieldHeight)
            .testTag("SearchField"),
        enabled = enabled,
        singleLine = true,
        textStyle = textStyle,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = query,
                innerTextField = innerTextField,
                enabled = enabled,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                interactionSource = interactionSource,
                placeholder =
                if (placeholder.isNotEmpty()) {
                    {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                        )
                    }
                } else {
                    null
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(Dimens2.inputIconSize)
                    )
                },
                trailingIcon =
                if (query.isNotEmpty()) {
                    {
                        IconButton(
                            onClick = onClearClick,
                            modifier = Modifier.testTag("ClearSearchButton")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = stringResource(R.string.search_clear_query),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(Dimens2.iconSize18)
                            )
                        }
                    }
                } else {
                    null
                },
                colors = textFieldColors,
                contentPadding =
                PaddingValues(
                    horizontal = Dimens2.inputHorizontalPadding,
                    vertical = Dimens2.zeroDp
                ),
                container = {
                    OutlinedTextFieldDefaults.Container(
                        enabled = enabled,
                        isError = false,
                        interactionSource = interactionSource,
                        colors = textFieldColors,
                        shape = MaterialTheme.shapes.large
                    )
                }
            )
        }
    )
}

@AppPreview
@Composable
private fun AppSearchBarPreview() {
    TrainingPlannerTheme2 {
        Column(
            modifier = Modifier.padding(Dimens2.spaceMd),
            verticalArrangement = Arrangement.spacedBy(Dimens2.spaceMd)
        ) {
            AppSearchBar(
                query = "",
                onQueryChange = {},
                placeholder = "Search Exercise"
            )
            AppSearchBar(
                query = "Pull-Up",
                onQueryChange = {},
                placeholder = "Search Exercise"
            )
        }
    }
}
