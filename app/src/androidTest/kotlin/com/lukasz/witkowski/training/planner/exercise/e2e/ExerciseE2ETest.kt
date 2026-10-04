package com.lukasz.witkowski.training.planner.exercise.e2e

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.printToLog
import com.lukasz.witkowski.training.planner.MainActivity
import com.lukasz.witkowski.training.planner.exercise.exerciseItemMatcher
import com.lukasz.witkowski.training.planner.exercise.givenExerciseItemTag
import com.lukasz.witkowski.training.planner.exercise.presentation.models.RecommendationLevel
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ExerciseE2ETest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val firstExerciseName = "90° Hold"
    private val secondExerciseName = "90° Push-Up"
    private val legExerciseName = "Bodyweight Squat"
    private val plankExerciseName = "Plank"
    private val createExerciseMatcher = hasContentDescription("Create exercise")
    private val legsCategoryName = "LEGS"
    private val chipLegsTag = "Chip$legsCategoryName, isClickable=true"

    @Before
    fun setUp() {
        waitForExercisesLoaded()
    }

    private fun waitForExercisesLoaded() {
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().printToLog(tag = "Root")
        composeTestRule.onRoot(useUnmergedTree = true).printToLog(tag = "RootUnmerged")
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodes(matcher = exerciseItemMatcher)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun testExerciseCreationRealE2E() {
        val exerciseName = "Real E2E Exercise"
        val exerciseDescription = "Real E2E Description"

        // Click FAB to create exercise
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodes(createExerciseMatcher)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule
            .onNode(createExerciseMatcher)
            .performClick()

        composeTestRule.waitForIdle()

        // Wait for form and category chips to load
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag(chipLegsTag)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Fill in name and description
        composeTestRule.onNodeWithTag("NameField").performTextInput(exerciseName)
        composeTestRule.onNodeWithTag("DescriptionField").performTextInput(exerciseDescription)

        composeTestRule.onNodeWithTag(chipLegsTag).performClick()

        // Expand recommendation card and fill valid parameters so save is enabled
        RecommendationLevel.entries.forEach {
            enterRecommendations(it)
        }
        performClickOnSaveButton()

        assertExerciseDetailsDisplayed(
            exerciseName = exerciseName,
            exerciseDescription = exerciseDescription,
            categories = listOf(legsCategoryName)
        )
    }

    @Test
    fun testExerciseEditRealE2E() {
        val originalName = firstExerciseName
        val updatedName = "$firstExerciseName Updated"

        whenGoToExerciseDetails(originalName)

        // Open overflow menu and click Edit
        composeTestRule.onNodeWithTag("OverflowMenuButton").performClick()
        composeTestRule.onNodeWithTag("MenuItem_Edit").performClick()
        composeTestRule.waitForIdle()

        // Update name
        composeTestRule.onNodeWithTag("NameField").performTextReplacement(updatedName)

        performClickOnSaveButton()

        assertExerciseDetailsDisplayed(
            exerciseName = updatedName
        )
    }

    @Test
    fun testExerciseFilteringRealE2E() {
        // Filter by query
        composeTestRule.onNodeWithTag("SearchField").performTextInput(plankExerciseName)
        composeTestRule.waitForIdle()
        assertExerciseDisplayedInList(plankExerciseName)

        // Clear query
        composeTestRule.onNodeWithTag("SearchField").performTextReplacement("")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(chipLegsTag).performClick()
        composeTestRule.waitForIdle()

        assertExerciseDisplayedInList(legExerciseName)
    }

    @Test
    fun testExerciseDeleteRealE2E() {
        whenGoToExerciseDetails(secondExerciseName)

        // Open overflow menu and click Delete
        composeTestRule.onNodeWithTag("OverflowMenuButton").performClick()
        composeTestRule.onNodeWithTag("MenuItem_Delete").performClick()
        composeTestRule.waitForIdle()

        // Confirm deletion in dialog
        composeTestRule.onNodeWithTag("confirmButton").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("ExercisesList").assertIsDisplayed()
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag(givenExerciseItemTag(secondExerciseName))
                .fetchSemanticsNodes()
                .isEmpty()
        }
        composeTestRule.onNodeWithTag(givenExerciseItemNameTag(secondExerciseName))
            .assertDoesNotExist()
    }


    private fun assertExerciseDisplayedInList(exerciseName: String) {
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag(givenExerciseItemTag(exerciseName))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(givenExerciseItemNameTag(exerciseName)).assertIsDisplayed()
    }

    private fun enterRecommendations(
        level: RecommendationLevel
    ) {
        val prefix = level.name
        composeTestRule.onNodeWithTag("ExerciseForm")
            .performScrollToNode(hasTestTag("${prefix}_CardHeader"))
        composeTestRule.onNodeWithTag("${prefix}_CardHeader").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("${prefix}_SetsField").performTextInput("3")
        composeTestRule.onNodeWithTag("${prefix}_RepsField").performTextInput("10")
        composeTestRule.onNodeWithTag("${prefix}_RestTimeField").performTextInput("60")
        composeTestRule.onNodeWithTag("${prefix}_WeightField").performTextInput("10")
    }

    private fun assertExerciseDetailsDisplayed(
        exerciseName: String,
        exerciseDescription: String? = null,
        categories: List<String> = emptyList(),
    ) {
        composeTestRule.onNodeWithTag("ExerciseDetails").assertIsDisplayed()
        composeTestRule.onNodeWithText(exerciseName).assertIsDisplayed()
        exerciseDescription?.let {
            composeTestRule.onNodeWithText(exerciseDescription).assertIsDisplayed()
        }
        categories.forEach {
            val categoryTag = "Chip$it, isClickable=false"
            composeTestRule.onNodeWithTag(categoryTag).assertIsDisplayed()
        }
    }

    private fun performClickOnSaveButton() {
        composeTestRule
            .onNodeWithTag("ExerciseForm")
            .performScrollToNode(hasTestTag("SaveButton"))

        composeTestRule.onNodeWithTag("SaveButton").performClick()
        composeTestRule.waitForIdle()
    }

    private fun whenGoToExerciseDetails(exerciseName: String) {
        composeTestRule
            .onNodeWithTag(givenExerciseItemTag(exerciseName))
            .performClick()
        composeTestRule.waitForIdle()
    }

    private fun givenExerciseItemNameTag(exerciseName: String) = "ExerciseItemName-$exerciseName"
}
