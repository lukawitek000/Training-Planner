package com.lukasz.witkowski.training.planner.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.presentation.FilterCategory
import com.lukasz.witkowski.training.planner.ui.theme.Dimens
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme

@Composable
fun SearchHeader(
    query: String,
    onSearchQueryChanged: (String) -> Unit,
    categories: List<FilterCategory>,
    toggleCategory: (ExerciseCategory) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        TextField(
            text = query,
            onTextChange = onSearchQueryChanged,
            label = label,
            modifier = Modifier
                .padding(Dimens.normal)
                .testTag("SearchField")
        )
        CategoryFilters(
            modifier = Modifier.padding(
                bottom = Dimens.normal,
                start = Dimens.normal,
                end = Dimens.normal
            ),
            categories = categories,
            toggleCategory = toggleCategory
        )
    }
}

data class FilteringState(
    val searchQuery: String,
    val categories: List<FilterCategory>
) {
    val isAnyCategorySelected = categories.any { it.isSelected }
}

@Preview
@Composable
fun SearchHeaderPreview() {
    TrainingPlannerTheme {
        SearchHeader(
            query = "",
            onSearchQueryChanged = {},
            categories = PREVIEW_CATEGORIES,
            toggleCategory = {},
            label = "Label"
        )
    }
}
