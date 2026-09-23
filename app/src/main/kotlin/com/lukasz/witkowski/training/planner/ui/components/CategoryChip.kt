package com.lukasz.witkowski.training.planner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme


@Composable
fun CategoryChip(
    modifier: Modifier = Modifier,
    category: Category,
    isSelected: Boolean = true,
    selectionChanged: (Boolean) -> Unit = {},
    isClickable: Boolean = false,
    fontSize: TextUnit = 16.sp
) {
    if (!category.isNone()) {
        val background = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
        val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
        Box(
            modifier = modifier
                .clip(shape = MaterialTheme.shapes.medium)
                .background(background)
                .then(if (isClickable) Modifier.clickable { selectionChanged(!isSelected) } else Modifier),
        ) {
            Text(
                text = stringResource(id = category.res),
                modifier = Modifier.padding(8.dp),
                fontSize = fontSize,
                textAlign = TextAlign.Center,
                color = textColor
            )
        }
    }
}


@Preview
@Composable
fun CategoryChipPreview() {
    TrainingPlannerTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CategoryChip(isSelected = true, category = Category(1, R.string.category_back), selectionChanged = { })
            CategoryChip(isSelected = false, category = Category(2, R.string.category_chest), selectionChanged = { })
        }
    }
}
