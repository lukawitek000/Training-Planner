package com.lukasz.witkowski.training.planner.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.models.Category
import com.lukasz.witkowski.training.planner.ui.theme.LightDark12
import com.lukasz.witkowski.training.planner.ui.theme.Orange


@Composable
fun CategoryChip(
    modifier: Modifier = Modifier,
    category: Category,
    isSelected: Boolean = true,
    selectionChanged: (Boolean) -> Unit = {},
    isClickable: Boolean = false,
    fontSize: TextUnit = 16.sp
) {
    val shape = RoundedCornerShape(16.dp)
    if (!category.isNone()) {
        Surface(
            modifier = modifier
                .clip(shape = shape)
                .then(if (isClickable) Modifier.clickable { selectionChanged(!isSelected) } else Modifier),
            elevation = 4.dp,
            color = if (isSelected) Orange else LightDark12
        ) {
            Text(
                text = stringResource(id = category.res),
                modifier = Modifier.padding(8.dp),
                fontSize = fontSize,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Preview
@Composable
fun CategoryChipPreview() {
    CategoryChip(isSelected = true, category = Category(1, R.string.category_back), selectionChanged = { })
}
