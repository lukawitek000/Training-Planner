package com.lukasz.witkowski.training.planner.exercise.createExercise

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.lukasz.witkowski.training.planner.exercise.TestData
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseCategoriesService
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.presentation.DefaultCategoryController2
import com.lukasz.witkowski.training.planner.exercise.presentation.FilterCategory
import com.lukasz.witkowski.training.planner.exercise.presentation.models.RecommendationLevel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class ExerciseEditorViewModelTest {
    private val service: ExerciseService = mockk()
    private lateinit var vm: ExerciseEditorViewModel

    private fun givenVm(exerciseId: ExerciseId? = null) {
        val categoryService = mockk<ExerciseCategoriesService>()
        every { categoryService.getAllCategories() } returns flowOf(TestData.CATEGORIES_LIST)
        vm = ExerciseEditorViewModel(
            exerciseService = service,
            categoryController = DefaultCategoryController2(
                categoryService = categoryService
            ),
            exerciseId = exerciseId
        )
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when exercise id is not null, the VM is in edit mode`() {
        givenVm(exerciseId = ExerciseId.create())
        assertTrue(vm.isEditMode)
    }

    @Test
    fun `when loading exercise to edit fails, failure state is emitted`() = runTest {
        val exerciseId = ExerciseId.create()
        val failureMessage = "Failure message"
        every { service.getExerciseDetailsById(any()) } returns flow {
            throw IllegalStateException(failureMessage)
        }
        givenVm(exerciseId = exerciseId)
        vm.uiState.test {
            assertEquals(ExerciseEditingUiState.Loading(exerciseId), awaitItem())
            assertEquals(ExerciseEditingUiState.Failure(null, failureMessage), awaitItem())
            assertEquals(ExerciseEditingUiState.Editing(ExerciseEditingInput()), awaitItem())
        }
    }

    @Test
    fun `when loading exercise to edit does not exist, failure state is emitted`() = runTest {
        val exerciseId = ExerciseId.create()
        val failureMessage = "Exercise not found"
        every { service.getExerciseDetailsById(any()) } returns flowOf(null)
        givenVm(exerciseId = exerciseId)
        vm.uiState.test {
            assertEquals(ExerciseEditingUiState.Loading(exerciseId), awaitItem())
            assertEquals(ExerciseEditingUiState.Failure(null, failureMessage), awaitItem())
        }
    }

    @Test
    fun `when loading exercise to edit is successful, edit state is emitted`() = runTest {
        val exerciseId = ExerciseId.create()
        val details = TestData.PUSH_UPS_DETAILS
        every { service.getExerciseDetailsById(any()) } returns flowOf(details)
        givenVm(exerciseId = exerciseId)
        vm.uiState.test {
            assertEquals(
                ExerciseEditingUiState.Loading(exerciseId),
                awaitItem()
            )
            val expectedInput = ExerciseEditingInput(
                name = details.exercise.name,
                description = details.exercise.description,
                recommendations = TestData.PRESENTATION_PUSH_UPS_DETAILS.recommendations
            )
            assertEquals(
                ExerciseEditingUiState.Editing(expectedInput),
                awaitItem()
            )
            assertEquals(
                ExerciseEditingUiState.Editing(
                    expectedInput.copy(
                        categories = details.exercise.categories.mapToFilteredCategories()
                    )
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `when exerciseId is null, the VM is in create mode`() {
        givenVm()
        assertFalse(vm.isEditMode)
    }

    @Test
    fun `when in create mode, initial state is empty edit input`() = runTest {
        givenVm()
        vm.uiState.test {
            assertInitialCreateState()
        }
    }

    @Test
    fun `when user changes name, the state reflects it`() = runTest {
        givenVm()
        vm.uiState.test {
            assertInitialCreateState()
            val new = "New name"
            vm.onEvent(ExerciseEditingEvent.NameChanged(new))
            assertEquals(
                ExerciseEditingUiState.Editing(ExerciseEditingInput(name = new)),
                awaitItem()
            )
        }
    }

    @Test
    fun `when user changes description, the state reflects it`() = runTest {
        givenVm()
        vm.uiState.test {
            assertInitialCreateState()
            val new = "New description"
            vm.onEvent(ExerciseEditingEvent.DescriptionChanged(new))
            assertEquals(
                ExerciseEditingUiState.Editing(ExerciseEditingInput(description = new)),
                awaitItem()
            )
        }
    }

    @Test
    fun `when user toggle category, the state reflects it`() = runTest {
        givenVm()
        vm.uiState.test {
            assertInitialCreateState()
            val categories = mutableListOf(TestData.CATEGORY_BICEPS)
            vm.onEvent(ExerciseEditingEvent.CategoryToggled(TestData.CATEGORY_BICEPS))
            assertEquals(
                ExerciseEditingUiState.Editing(
                    ExerciseEditingInput(
                        categories = categories.mapToFilteredCategories()
                    )
                ),
                awaitItem()
            )
            categories.addFirst(TestData.CATEGORY_CHEST)
            vm.onEvent(ExerciseEditingEvent.CategoryToggled(TestData.CATEGORY_CHEST))
            assertEquals(
                ExerciseEditingUiState.Editing(
                    ExerciseEditingInput(
                        categories = categories.mapToFilteredCategories()
                    )
                ),
                awaitItem()
            )
            categories.remove(TestData.CATEGORY_CHEST)
            vm.onEvent(ExerciseEditingEvent.CategoryToggled(TestData.CATEGORY_CHEST))
            assertEquals(
                ExerciseEditingUiState.Editing(
                    ExerciseEditingInput(
                        categories = categories.mapToFilteredCategories()
                    )
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `when user changes recommended sets, the state reflects it`() = runTest {
        givenVm()
        vm.uiState.test {
            assertInitialCreateState()
            val newSets = 4
            val level = RecommendationLevel.BEGINNER
            vm.onEvent(ExerciseEditingEvent.RecommendedSetsChanged(newSets, level))

            val expectedRecommendations = ExerciseEditingInput().recommendations.map {
                if (it.level == level) {
                    it.copy(parameters = it.parameters.copy(sets = newSets))
                } else {
                    it
                }
            }
            assertEquals(
                ExerciseEditingUiState.Editing(
                    ExerciseEditingInput(recommendations = expectedRecommendations)
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `when user changes recommended reps, the state reflects it`() = runTest {
        givenVm()
        vm.uiState.test {
            assertInitialCreateState()
            val newReps = 12
            val level = RecommendationLevel.INTERMEDIATE
            vm.onEvent(ExerciseEditingEvent.RecommendedRepsChanged(newReps, level))

            val expectedRecommendations = ExerciseEditingInput().recommendations.map {
                if (it.level == level) {
                    it.copy(parameters = it.parameters.copy(reps = newReps))
                } else {
                    it
                }
            }
            assertEquals(
                ExerciseEditingUiState.Editing(
                    ExerciseEditingInput(recommendations = expectedRecommendations)
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `when user changes recommended rest time, the state reflects it`() = runTest {
        givenVm()
        vm.uiState.test {
            assertInitialCreateState()
            val newRestTime = 60.seconds
            val level = RecommendationLevel.ADVANCED
            vm.onEvent(ExerciseEditingEvent.RecommendedRestTimeChanged(newRestTime, level))

            val expectedRecommendations = ExerciseEditingInput().recommendations.map {
                if (it.level == level) {
                    it.copy(parameters = it.parameters.copy(restTime = newRestTime))
                } else {
                    it
                }
            }
            assertEquals(
                ExerciseEditingUiState.Editing(
                    ExerciseEditingInput(recommendations = expectedRecommendations)
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `when user changes recommended weight, the state reflects it`() = runTest {
        givenVm()
        vm.uiState.test {
            assertInitialCreateState()
            val newWeight = 15
            val level = RecommendationLevel.BEGINNER
            vm.onEvent(ExerciseEditingEvent.RecommendedWeightChanged(newWeight, level))

            val expectedRecommendations = ExerciseEditingInput().recommendations.map {
                if (it.level == level) {
                    it.copy(parameters = it.parameters.copy(weightInKg = newWeight))
                } else {
                    it
                }
            }
            assertEquals(
                ExerciseEditingUiState.Editing(
                    ExerciseEditingInput(recommendations = expectedRecommendations)
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `when save exercise is requested and succeeds, saving and saved states are emitted`() = runTest {
        val newExerciseId = TestData.EXERCISE_ID_1
        coEvery { service.saveExercise(any()) } returns newExerciseId
        givenVm()
        val input = ExerciseEditingInput(
            name = "Push-up",
            recommendations = TestData.PRESENTATION_PUSH_UPS_DETAILS.recommendations
        )

        vm.uiState.test {
            assertInitialCreateState()
            vm.onEvent(ExerciseEditingEvent.SaveChangesRequested(input))

            assertEquals(ExerciseEditingUiState.Saving(input), awaitItem())
            assertEquals(ExerciseEditingUiState.Saved(newExerciseId, input.name), awaitItem())
        }
        coVerify(exactly = 1) { service.saveExercise(any()) }
    }

    @Test
    fun `when save exercise is requested and fails, saving, failure and editing states are emitted`() = runTest {
        val errorMessage = "Failed to save exercise"
        coEvery { service.saveExercise(any()) } throws IllegalStateException(errorMessage)
        givenVm()
        val input = ExerciseEditingInput(
            name = "Push-up",
            recommendations = TestData.PRESENTATION_PUSH_UPS_DETAILS.recommendations
        )

        vm.uiState.test {
            assertInitialCreateState()
            vm.onEvent(ExerciseEditingEvent.SaveChangesRequested(input))

            assertEquals(ExerciseEditingUiState.Saving(input), awaitItem())
            assertEquals(ExerciseEditingUiState.Failure(input, errorMessage), awaitItem())
            assertEquals(ExerciseEditingUiState.Editing(input), awaitItem())
        }
        coVerify(exactly = 1) { service.saveExercise(any()) }
    }

    @Test
    fun `when update exercise in edit mode is requested and succeeds, saving and saved states are emitted`() = runTest {
        val exerciseId = TestData.EXERCISE_ID_1
        val details = TestData.PUSH_UPS_DETAILS
        every { service.getExerciseDetailsById(exerciseId) } returns flowOf(details)
        coEvery { service.updateExercise(any(), exerciseId) } returns exerciseId
        givenVm(exerciseId = exerciseId)

        val input = ExerciseEditingInput(
            name = "Updated Push-up",
            recommendations = TestData.PRESENTATION_PUSH_UPS_DETAILS.recommendations
        )

        vm.uiState.test {
            skipItems(3) // Loading, Editing (initial input), Editing (with filtered categories)
            vm.onEvent(ExerciseEditingEvent.SaveChangesRequested(input))

            assertEquals(ExerciseEditingUiState.Saving(input), awaitItem())
            assertEquals(ExerciseEditingUiState.Saved(exerciseId, input.name), awaitItem())
        }
        coVerify(exactly = 1) { service.updateExercise(any(), exerciseId) }
    }

    @Test
    fun `when update exercise in edit mode is requested and fails, saving, failure and editing states are emitted`() = runTest {
        val exerciseId = TestData.EXERCISE_ID_1
        val details = TestData.PUSH_UPS_DETAILS
        val errorMessage = "Failed to update exercise"
        every { service.getExerciseDetailsById(exerciseId) } returns flowOf(details)
        coEvery { service.updateExercise(any(), exerciseId) } throws IllegalStateException(errorMessage)
        givenVm(exerciseId = exerciseId)

        val input = ExerciseEditingInput(
            name = "Updated Push-up",
            recommendations = TestData.PRESENTATION_PUSH_UPS_DETAILS.recommendations
        )

        vm.uiState.test {
            skipItems(3) // Loading, Editing (initial input), Editing (with filtered categories)
            vm.onEvent(ExerciseEditingEvent.SaveChangesRequested(input))

            assertEquals(ExerciseEditingUiState.Saving(input), awaitItem())
            assertEquals(ExerciseEditingUiState.Failure(input, errorMessage), awaitItem())
            assertEquals(ExerciseEditingUiState.Editing(input), awaitItem())
        }
        coVerify(exactly = 1) { service.updateExercise(any(), exerciseId) }
    }



    private suspend fun ReceiveTurbine<ExerciseEditingUiState>.assertInitialCreateState() {
        assertEquals(ExerciseEditingUiState.Editing(ExerciseEditingInput()), awaitItem())
    }

    private fun Collection<ExerciseCategory>.mapToFilteredCategories(): List<FilterCategory> {
        return map {
            FilterCategory(it, isSelected = true)
        } + TestData.CATEGORIES_LIST.filter {
            !contains(
                it
            )
        }.map {
            FilterCategory(it, isSelected = false)
        }
    }

}