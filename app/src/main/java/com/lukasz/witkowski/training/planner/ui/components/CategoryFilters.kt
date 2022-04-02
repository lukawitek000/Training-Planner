package com.lukasz.witkowski.training.planner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category

@Composable
fun CategoryFilters(
    modifier: Modifier = Modifier,
    categories: List<Category>,
    selectedCategories: List<Category>,
    selectCategory: (Category) -> Unit
) {
    LazyRow(
        modifier = modifier.padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(categories) { category ->
            CategoryChip(
                modifier = Modifier.padding(4.dp),
                isSelected = selectedCategories.any { it == category },
                category = category,
                selectionChanged = { selectCategory(category) },
                isClickable = true
            )
        }
    }
}