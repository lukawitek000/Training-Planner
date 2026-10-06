package com.lukasz.witkowski.training.planner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lukasz.witkowski.training.planner.ui.theme.Dimens


@Composable
fun CategoriesRow(
    categories: List<String>,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Dimens.normal),
    ) {
        items(categories) { item ->
            CategoryChip(
                modifier = Modifier,
                category = item,
            )
        }
    }
}