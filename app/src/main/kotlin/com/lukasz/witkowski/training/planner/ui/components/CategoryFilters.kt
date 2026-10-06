package com.lukasz.witkowski.training.planner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.presentation.FilterCategory
import com.lukasz.witkowski.training.planner.ui.theme.Dimens
import com.lukasz.witkowski.training.planner.ui.theme.TrainingPlannerTheme

@Composable
fun CategoryFilters(
    categories: List<FilterCategory>,
    toggleCategory: (ExerciseCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.normal)
    ) {
        items(categories) { category ->
            CategoryChip(
                modifier = Modifier,
                isSelected = category.isSelected,
                category = category.category,
                onClick = { toggleCategory(category.category) },
                isClickable = true
            )
        }
    }
}

@Preview
@Composable
private fun CategoryFiltersPreview() {
    TrainingPlannerTheme {
        CategoryFilters(
            categories = PREVIEW_CATEGORIES,
            toggleCategory = {}
        )
    }
}

val PREVIEW_CATEGORIES = listOf(
    FilterCategory(ExerciseCategory("Back"), isSelected = false),
    FilterCategory(ExerciseCategory("Leg"), isSelected = true),
    FilterCategory(ExerciseCategory("Arm"), isSelected = false),
    FilterCategory(ExerciseCategory("Biceps"), isSelected = false),
    FilterCategory(ExerciseCategory("Abs"), isSelected = false),
    FilterCategory(ExerciseCategory("Cardio"), isSelected = false),
    FilterCategory(ExerciseCategory("Shoulder"), isSelected = false),
    FilterCategory(ExerciseCategory("Neck"), isSelected = false),
)