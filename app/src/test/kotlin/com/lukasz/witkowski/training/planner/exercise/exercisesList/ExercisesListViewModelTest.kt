package com.lukasz.witkowski.training.planner.exercise.exercisesList

import androidx.paging.PagingData
import app.cash.turbine.test
import com.lukasz.witkowski.training.planner.exercise.TestData
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseCategoriesService
import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseQuery
import com.lukasz.witkowski.training.planner.exercise.presentation.DefaultCategoryController2
import com.lukasz.witkowski.training.planner.exercise.presentation.FilterCategory
import com.lukasz.witkowski.training.planner.ui.components.FilteringState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class ExercisesListViewModelTest {
    private val categories = listOf(
        ExerciseCategory("Back"),
        ExerciseCategory("Abs"),
        ExerciseCategory("Leg"),
    )
    private val service: ExerciseService = mockk()
    private val categoryService: ExerciseCategoriesService = mockk()
    private lateinit var vm: ExercisesListViewModel

    @Before
    fun setUp() {
        every { categoryService.getAllCategories() } returns flow { emit(categories) }
        val testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        vm = ExercisesListViewModel(
            exerciseService = service,
            categoryController2 = DefaultCategoryController2(
                categoryService = categoryService
            )
        )
    }

    @After
    fun tearDonw() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when search query is changed new filtering state is emitted`() = runTest {
        vm.filteringState.test {
            assertEquals(
                expected = FilteringState("", emptyList()),
                actual = awaitItem()
            )
            val testQuery = "Test query"
            vm.onSearchQueryChange(testQuery)
            assertEquals(
                expected = FilteringState(testQuery, categories.toFilterCategories()),
                actual = awaitItem()
            )
            vm.toggleCategory(categories.first())
            assertEquals(
                expected = FilteringState(
                    testQuery,
                    categories.toFilterCategories(categories.take(1))
                ),
                actual = awaitItem()
            )
            val newTextQuery = "newTextQuery"
            vm.onSearchQueryChange(newTextQuery)
            assertEquals(
                expected = FilteringState(
                    newTextQuery,
                    categories.toFilterCategories(categories.take(1))
                ),
                actual = awaitItem()
            )
            vm.toggleCategory(categories.first())
            assertEquals(
                expected = FilteringState(
                    newTextQuery,
                    categories.toFilterCategories()
                ),
                actual = awaitItem()
            )
        }
    }

    @Test
    fun `when query change too often the debounce avoid too often execution`() = runTest {
        coEvery { service.queryExercises(any()) } returns
                flowOf(PagingData.from(TestData.EXERCISES_LIST))

        vm.exercises.test {
            vm.onSearchQueryChange("P")
            advanceTimeBy(100.milliseconds)
            vm.onSearchQueryChange("Pus")
            advanceTimeBy(100.milliseconds)
            vm.onSearchQueryChange("Push")
            advanceTimeBy(301.milliseconds)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { service.queryExercises(ExerciseQuery(emptyList(), "Push")) }
        coVerify(exactly = 0) { service.queryExercises(ExerciseQuery(emptyList(), "Pus")) }
        coVerify(exactly = 0) { service.queryExercises(ExerciseQuery(emptyList(), "P")) }
    }

    @Test
    fun `when search query change while query is executed request is cancelled`() = runTest {
        val firstQuery = "Pull"
        var isFirstQueryCancelled = false
        coEvery { service.queryExercises(match { it.query ==  firstQuery}) } returns flow {
            delay(302.milliseconds)
            emit(PagingData.from(TestData.EXERCISES_LIST))
        }.onCompletion {
            if (it is CancellationException) {
                isFirstQueryCancelled = true
            }
        }
        val secondQuery = "Push"
        var isSecondQueryCancelled = false
        coEvery { service.queryExercises(match { it.query == secondQuery}) } returns flow {
            emit(PagingData.from(TestData.EXERCISES_LIST))
        }.onCompletion {
            if (it is CancellationException) {
                isSecondQueryCancelled = true
            }
        }

        vm.exercises.test {
            vm.onSearchQueryChange(firstQuery)
            advanceTimeBy(301.milliseconds)

            vm.onSearchQueryChange(secondQuery)
            advanceTimeBy(301.milliseconds)

            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { service.queryExercises(ExerciseQuery(emptyList(), firstQuery)) }
        coVerify(exactly = 1) { service.queryExercises(ExerciseQuery(emptyList(), secondQuery)) }
        assertTrue(isFirstQueryCancelled)
        assertFalse(isSecondQueryCancelled)
    }

    private fun List<ExerciseCategory>.toFilterCategories(
        selected: List<ExerciseCategory> = emptyList()
    ): List<FilterCategory> {
        return map { FilterCategory(it, isSelected = selected.contains(it)) }
    }
}