package com.lukasz.witkowski.training.planner.exercise.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsSelectable
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.printToLog
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.lukasz.witkowski.training.planner.MainActivity
import com.lukasz.witkowski.training.planner.exercise.exerciseItemMatcher
import com.lukasz.witkowski.training.planner.exercise.givenExerciseItemTag
import com.lukasz.witkowski.training.planner.exercise.presentation.models.RecommendationLevel
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

class ExerciseE2ETest {
    companion object {
        @JvmStatic
        @BeforeClass
        fun clearDatabase() {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            context.deleteDatabase("ExerciseDb")
        }
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val legExerciseName = "Bodyweight Squat"
    private val plankExerciseName = "Plank"
    private val createExerciseMatcher = hasContentDescription("Create exercise")
    private val legsCategoryName = "LEGS"
    private val chipLegsTag = "Chip$legsCategoryName, isClickable=true"

    @Before
    fun setUp() {
        navigateToExercisesTab()
        waitForExercisesLoaded()
    }

    private fun navigateToExercisesTab() {
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag("BottomNavItem_1")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag("BottomNavItem_1").performClick()
        composeTestRule.waitForIdle()
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

        enterExerciseConfigurationOnCreation(exerciseName, exerciseDescription)
        performClickOnSaveButton()

        assertExerciseDetailsDisplayed(
            exerciseName = exerciseName,
            exerciseDescription = exerciseDescription,
            categories = listOf(legsCategoryName)
        )
    }



    @Test
    fun testExerciseEditRealE2E() {
        val originalName = "Editable Custom Exercise"
        val updatedName = "$originalName Updated"

        createCustomExercise(originalName)

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
        composeTestRule.onNodeWithTag("SearchField").performImeAction()
        composeTestRule.waitForIdle()
        assertExerciseDisplayedInList(plankExerciseName)

        // Clear query
        composeTestRule.onNodeWithTag("SearchField").performTextReplacement("")
        composeTestRule.onNodeWithTag("SearchField").performImeAction()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(chipLegsTag).performClick()
        composeTestRule.waitForIdle()

        assertExerciseDisplayedInList(legExerciseName)
    }

    @Test
    fun testExerciseDeleteRealE2E() {
        val exerciseToDelete = "Deletable Custom Exercise"

        createCustomExercise(exerciseToDelete)

        whenGoToExerciseDetails(exerciseToDelete)

        // Open overflow menu and click Delete
        composeTestRule.onNodeWithTag("OverflowMenuButton").performClick()
        composeTestRule.onNodeWithTag("MenuItem_Delete").performClick()
        composeTestRule.waitForIdle()

        // Confirm deletion in dialog
        composeTestRule.onNodeWithTag("confirmButton").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag("NoDataMessage")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun createCustomExercise(name: String, description: String = "Test Description") {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodes(createExerciseMatcher)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule
            .onNode(createExerciseMatcher)
            .performClick()

        enterExerciseConfigurationOnCreation(name, description)
        performClickOnSaveButton()

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag("ExerciseDetails")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNode(hasContentDescription("Go back")).performClick()
        composeTestRule.waitForIdle()
    }

    private fun enterExerciseConfigurationOnCreation(exerciseName: String, exerciseDescription: String) {
        composeTestRule.waitForIdle()
        // Fill in name and description
        composeTestRule.onNodeWithTag("NameField").performTextReplacement(exerciseName)
        composeTestRule.onNodeWithTag("DescriptionField").performTextReplacement(exerciseDescription)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("ExerciseForm")
            .performScrollToNode(hasTestTag(chipLegsTag))
        composeTestRule.onNodeWithTag(chipLegsTag)
            .assertExists()
            .assertIsSelectable()
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(chipLegsTag)
            .assertIsSelected()

        // Expand recommendation card and fill valid parameters so save is enabled
        RecommendationLevel.entries.forEach {
            enterRecommendations(it)
        }
    }

    private fun assertExerciseDisplayedInList(exerciseName: String) {
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag(givenExerciseItemTag(exerciseName))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule
            .onNodeWithTag("ExercisesList")
            .performScrollToNode(hasTestTag(givenExerciseItemTag(exerciseName)))
        composeTestRule.onNodeWithTag(givenExerciseItemTag(exerciseName)).assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(givenExerciseItemNameTag(exerciseName), useUnmergedTree = true)
            .assertExists()
    }

    private fun enterRecommendations(
        level: RecommendationLevel
    ) {
        val prefix = level.name
        composeTestRule.onNodeWithTag("ExerciseForm")
            .performScrollToNode(hasTestTag("${prefix}_CardHeader"))
        composeTestRule.onNodeWithTag("${prefix}_CardHeader").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("ExerciseForm")
            .performScrollToNode(hasTestTag("${prefix}_SetsField"))
        composeTestRule.onNodeWithTag("${prefix}_SetsField").performTextReplacement("3")

        composeTestRule.onNodeWithTag("ExerciseForm")
            .performScrollToNode(hasTestTag("${prefix}_RepsField"))
        composeTestRule.onNodeWithTag("${prefix}_RepsField").performTextReplacement("10")

        composeTestRule.onNodeWithTag("ExerciseForm")
            .performScrollToNode(hasTestTag("${prefix}_RestTimeField"))
        composeTestRule.onNodeWithTag("${prefix}_RestTimeField").performTextReplacement("60")

        composeTestRule.onNodeWithTag("ExerciseForm")
            .performScrollToNode(hasTestTag("${prefix}_WeightField"))
        composeTestRule.onNodeWithTag("${prefix}_WeightField").performTextReplacement("10")

        // Collapse card after filling to keep ExerciseForm layout compact
        composeTestRule.onNodeWithTag("ExerciseForm")
            .performScrollToNode(hasTestTag("${prefix}_CardHeader"))
        composeTestRule.onNodeWithTag("${prefix}_CardHeader").performClick()
        composeTestRule.waitForIdle()
    }

    private fun assertExerciseDetailsDisplayed(
        exerciseName: String,
        exerciseDescription: String? = null,
        categories: List<String> = emptyList(),
    ) {
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag("ExerciseDetails")
                .fetchSemanticsNodes()
                .isNotEmpty() &&
                    composeTestRule
                        .onAllNodesWithText(exerciseName)
                        .fetchSemanticsNodes()
                        .isNotEmpty()
        }
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
        Espresso.closeSoftKeyboard()
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag("ExerciseForm")
            .performScrollToNode(hasTestTag("SaveButton"))

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("SaveButton").assertIsEnabled()
        composeTestRule.onNodeWithTag("SaveButton").performClick()
        composeTestRule.waitForIdle()
    }

    private fun whenGoToExerciseDetails(exerciseName: String) {
        composeTestRule.onNodeWithTag("SearchField").performTextInput(exerciseName)
        composeTestRule.onNodeWithTag("SearchField").performImeAction()
        composeTestRule.waitForIdle()
        composeTestRule
            .onAllNodesWithTag(givenExerciseItemTag(exerciseName))
            .fetchSemanticsNodes()
            .isNotEmpty()
        composeTestRule
            .onNodeWithTag("ExercisesList")
            .performScrollToNode(hasTestTag(givenExerciseItemTag(exerciseName)))
        composeTestRule
            .onNodeWithTag(givenExerciseItemTag(exerciseName))
            .performClick()
        composeTestRule.waitForIdle()
    }

    private fun givenExerciseItemNameTag(exerciseName: String) = "ExerciseItemName-$exerciseName"
}
