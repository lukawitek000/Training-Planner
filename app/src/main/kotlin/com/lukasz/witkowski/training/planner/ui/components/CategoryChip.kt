package com.lukasz.witkowski.training.planner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category
import com.lukasz.witkowski.training.planner.ui.theme.Dimens
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme

@Composable
fun CategoryChips(
    categories: List<ExerciseCategory>,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.normal)
    ) {
        items(categories) { category ->
            CategoryChip(
                modifier = Modifier,
                category = category,
            )
        }
    }
}

@Composable
fun CategoryChip(
    modifier: Modifier = Modifier,
    category: Category,
    isSelected: Boolean = true,
    selectionChanged: (Boolean) -> Unit = {},
    isClickable: Boolean = false,
) {
    if (!category.isNone()) {
        CategoryChip(
            modifier = modifier,
            category = ExerciseCategory(stringResource(category.res)),
            isSelected = isSelected,
            onClick = { selectionChanged(!isSelected) },
            isClickable = isClickable
        )
    }
}

@Composable
fun CategoryChip(
    modifier: Modifier = Modifier,
    category: ExerciseCategory,
    isSelected: Boolean = true,
    onClick: () -> Unit = {},
    isClickable: Boolean = false,
) {
    val background =
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
    val textColor =
        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
    Box(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.medium)
            .background(background)
            .then(if (isClickable) Modifier.clickable { onClick() } else Modifier),
    ) {
        Text(
            text = category.name,
            modifier = Modifier.padding(Dimens.normal),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = textColor
        )
    }
}


@Preview
@Composable
fun CategoryChipPreview() {
    TrainingPlannerTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.normal)) {
            CategoryChip(
                isSelected = true,
                category = Category(1, R.string.category_back),
                selectionChanged = { })
            CategoryChip(
                isSelected = false,
                category = Category(2, R.string.category_chest),
                selectionChanged = { })
        }
    }
}
