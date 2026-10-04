package com.lukasz.witkowski.training.planner.exercise.edit

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.lukasz.witkowski.training.planner.exercise.createExercise.CreateExerciseScreenContent
import com.lukasz.witkowski.training.planner.exercise.createExercise.ExerciseEditingEvent
import com.lukasz.witkowski.training.planner.exercise.createExercise.ExerciseEditingInput
import com.lukasz.witkowski.training.planner.exercise.createExercise.ExerciseEditingUiState
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.presentation.FilterCategory
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Recommendation
import com.lukasz.witkowski.training.planner.exercise.presentation.models.RecommendationLevel
import com.lukasz.witkowski.training.planner.exercise.presentation.models.RecommendedParameters
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class CreateExerciseScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loading_exercise_is_properly_displayed() {
        val id = ExerciseId.create()
        composeTestRule.setContent {
            CreateExerciseScreenContent(
                uiState = ExerciseEditingUiState.Loading(id),
                onUserInputChange = {},
                onExerciseSaved = {},
                isEditMode = false,
            )
        }

        composeTestRule
            .onNodeWithTag("loading")
            .assertIsDisplayed()
    }

    @Test
    fun saving_exercise_is_properly_displayed() {
        composeTestRule.setContent {
            CreateExerciseScreenContent(
                uiState = ExerciseEditingUiState.Saving(ExerciseEditingInput()),
                onUserInputChange = {},
                onExerciseSaved = {},
                isEditMode = false,
            )
        }

        composeTestRule
            .onNodeWithTag("OverlayContent")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("Loading")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("ExerciseForm")
            .assertIsDisplayed()
    }

    @Test
    fun failure_state_is_properly_displayed_for_create() {
        val failureMessage = "Hello"
        composeTestRule.setContent {
            CreateExerciseScreenContent(
                uiState = ExerciseEditingUiState.Failure(ExerciseEditingInput(), failureMessage),
                onUserInputChange = {},
                onExerciseSaved = {},
                isEditMode = false,
            )
        }

        composeTestRule
            .onNodeWithTag("OverlayContent")
            .assertIsNotDisplayed()

        composeTestRule
            .onNodeWithTag("Loading")
            .assertIsNotDisplayed()
        composeTestRule
            .onNodeWithTag("ExerciseForm")
            .assertIsDisplayed()
    }

    @Test
    fun editing_state_is_properly_displayed() = runTest {
        val name = "Name1"
        val description = "Descrp"
        val newName = "newName"
        val newDescription = "newDescription"
        val category1 = ExerciseCategory("Back")
        val category2 = ExerciseCategory("Chest")
        val inputEvents = mutableListOf<ExerciseEditingEvent>()
        val recommendations = listOf(
            Recommendation(
                RecommendationLevel.BEGINNER,
                parameters = RecommendedParameters(sets = 3, reps = 10, restTime = 30.seconds, weightInKg = 20)
            )
        )
        val input = ExerciseEditingInput(
            name = name,
            description = description,
            categories = listOf(
                FilterCategory(category1, isSelected = true),
                FilterCategory(category2, isSelected = false)
            ),
            recommendations = recommendations
        )
        composeTestRule.setContent {
            CreateExerciseScreenContent(
                uiState = ExerciseEditingUiState.Editing(input),
                onUserInputChange = { inputEvents.add(it) },
                onExerciseSaved = {},
                isEditMode = false,
            )
        }
        composeTestRule
            .onNodeWithTag("OverlayContent")
            .assertIsNotDisplayed()
        composeTestRule
            .onNodeWithTag("Loading")
            .assertIsNotDisplayed()

        composeTestRule
            .onNodeWithTag("ExerciseForm")
            .assertIsDisplayed()

        // 1. Name Field
        val nameNode = composeTestRule
            .onNodeWithTag("NameField")
        val actualName = nameNode
            .fetchSemanticsNode()
            .config[SemanticsProperties.EditableText]
            .text

        assertEquals(name, actualName)
        nameNode.performTextReplacement(newName)

        // 2. Description Field
        val descriptionNode = composeTestRule
            .onNodeWithTag("DescriptionField")
        val actualDescription = descriptionNode
            .fetchSemanticsNode()
            .config[SemanticsProperties.EditableText]
            .text

        assertEquals(description, actualDescription)
        descriptionNode.performTextReplacement(newDescription)

        // 3. Category Chip
        val categoryNode = composeTestRule
            .onNodeWithTag("ChipChest, isClickable=true")
        categoryNode.assertIsDisplayed()
        categoryNode.performClick()

        // 4. Recommendations Card (Beginner) header overview and expansion
        val cardHeaderNode = composeTestRule
            .onNodeWithTag("BEGINNER_CardHeader")
        cardHeaderNode.assertIsDisplayed()

        // Test that the header overview displays correct data
        composeTestRule.onNodeWithText("3 x 10 · 30 s · 20 kg").assertIsDisplayed()

        // Expand the recommendation card to show input fields
        cardHeaderNode.performClick()
        composeTestRule.waitForIdle()

        val setsNode = composeTestRule
            .onNodeWithTag("BEGINNER_SetsField")
        setsNode.performTextReplacement("5")

        val repsNode = composeTestRule
            .onNodeWithTag("BEGINNER_RepsField")
        repsNode.performTextReplacement("15")

        val restTimeNode = composeTestRule
            .onNodeWithTag("BEGINNER_RestTimeField")
        restTimeNode.performTextReplacement("60")

        val weightNode = composeTestRule
            .onNodeWithTag("BEGINNER_WeightField")
        weightNode.performTextReplacement("40")

        // 5. Save Button
        val saveButton = composeTestRule
            .onNodeWithTag("SaveButton")
        saveButton.performClick()

        assertEquals(true, inputEvents.contains(ExerciseEditingEvent.NameChanged(newName)))
        assertEquals(true, inputEvents.contains(ExerciseEditingEvent.DescriptionChanged(newDescription)))
        assertEquals(true, inputEvents.contains(ExerciseEditingEvent.CategoryToggled(category2)))
        assertEquals(true, inputEvents.contains(ExerciseEditingEvent.RecommendedSetsChanged(5, RecommendationLevel.BEGINNER)))
        assertEquals(true, inputEvents.contains(ExerciseEditingEvent.RecommendedRepsChanged(15, RecommendationLevel.BEGINNER)))
        assertEquals(true, inputEvents.contains(ExerciseEditingEvent.RecommendedRestTimeChanged(60.seconds, RecommendationLevel.BEGINNER)))
        assertEquals(true, inputEvents.contains(ExerciseEditingEvent.RecommendedWeightChanged(40, RecommendationLevel.BEGINNER)))
        assertEquals(true, inputEvents.contains(ExerciseEditingEvent.SaveChangesRequested(input)))
    }

    @Test
    fun saved_state_triggers_on_exercise_saved() {
        val exerciseId = ExerciseId.create()
        var savedExerciseId: ExerciseId? = null
        composeTestRule.setContent {
            CreateExerciseScreenContent(
                uiState = ExerciseEditingUiState.Saved(exerciseId, name = "Test Exercise"),
                onUserInputChange = {},
                onExerciseSaved = { savedExerciseId = it },
                isEditMode = false,
            )
        }
        assertEquals(exerciseId, savedExerciseId)
    }
}
