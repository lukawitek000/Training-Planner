package com.lukasz.witkowski.training.planner.exercise.list

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelectable
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.paging.compose.collectAsLazyPagingItems
import com.lukasz.witkowski.training.planner.exercise.TestData
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.exerciseItemMatcher
import com.lukasz.witkowski.training.planner.exercise.exercisesList.ExercisesScreenContent
import com.lukasz.witkowski.training.planner.exercise.exercisesList.asPreviewPagerFlow
import com.lukasz.witkowski.training.planner.exercise.givenExerciseItemTag
import com.lukasz.witkowski.training.planner.exercise.presentation.FilterCategory
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise2
import com.lukasz.witkowski.training.planner.ui.components.FilteringState
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ExercisesScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun lambdas_are_properly_trigger_on_user_action() {
        val exercisesList = listOf(TestData.PRESENTATION_PUSH_UPS_EXERCISE)
        var categoryTrigger: ExerciseCategory? = null
        var query = ""
        var exerciseClickedId: ExerciseId? = null
        val expectedQuery = "Hello"
        val expectedCategory = TestData.CATEGORY_BICEPS

        composeTestRule.setContent {
            ExercisesScreenContent(
                exercisesList = exercisesList.asPreviewPagerFlow().collectAsLazyPagingItems(),
                filteringState = FilteringState(
                    searchQuery = "",
                    categories = TestData.CATEGORIES_LIST.map {
                        FilterCategory(it, isSelected = it == expectedCategory)
                    }
                ),
                toggleCategory = {
                    categoryTrigger = it
                },
                onSearchQueryChanged = {
                    query = it
                },
                onExerciseClicked = {
                    exerciseClickedId = it
                }
            )
        }

        composeTestRule
            .onNodeWithTag(givenExerciseItemTag(exercisesList.first().name))
            .performClick()

        assertEquals(exercisesList.first().id, exerciseClickedId)

        composeTestRule
            .onNodeWithTag("SearchField")
            .performTextInput(expectedQuery)

        assertEquals(expectedQuery, query)

        val chipTag = "Chip${expectedCategory.name}, isClickable=true"

        composeTestRule
            .onNodeWithTag(chipTag)
            .assertIsSelectable()
            .assertIsSelected()
            .performClick()

        assertEquals(expectedCategory, categoryTrigger)

        composeTestRule
            .onNodeWithTag("Chip${TestData.CATEGORY_LEGS.name}, isClickable=true")
            .assertIsNotSelected()
    }

    @Test
    fun no_data_message_is_displayed_when_no_data_is_provided() {
        val exercisesList = emptyList<Exercise2>()
        composeTestRule.setContent {
            ExercisesScreenContent(
                exercisesList = exercisesList.asPreviewPagerFlow().collectAsLazyPagingItems(),
                filteringState = emptyFilteringState(),
                toggleCategory = {},
                onSearchQueryChanged = {},
                onExerciseClicked = {}
            )
        }

        composeTestRule
            .onNodeWithTag("NoDataMessage")
            .assertIsDisplayed()
    }

    @Test
    fun exercise_list_is_displayed_when_data_is_provided() {
        val exercisesList = listOf(TestData.PRESENTATION_PUSH_UPS_EXERCISE)

        composeTestRule.setContent {
            ExercisesScreenContent(
                exercisesList = exercisesList.asPreviewPagerFlow().collectAsLazyPagingItems(),
                filteringState = emptyFilteringState(),
                toggleCategory = {},
                onSearchQueryChanged = {},
                onExerciseClicked = {}
            )
        }

        composeTestRule
            .onNodeWithTag("NoDataMessage")
            .assertDoesNotExist()

        composeTestRule
            .onAllNodes(matcher = exerciseItemMatcher)
            .assertCountEquals(1)
    }

    private fun emptyFilteringState() = FilteringState("", emptyList())
}