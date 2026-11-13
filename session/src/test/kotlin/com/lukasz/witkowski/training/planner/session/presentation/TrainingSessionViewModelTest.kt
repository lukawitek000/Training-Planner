package com.lukasz.witkowski.training.planner.session.presentation

import com.lukasz.witkowski.training.planner.exercise.application.ExerciseService
import com.lukasz.witkowski.training.planner.image.ImageId
import com.lukasz.witkowski.training.planner.image.ImageReference
import com.lukasz.witkowski.training.planner.session.TRAINING_PLAN
import com.lukasz.witkowski.training.planner.session.application.TrainingSessionService
import com.lukasz.witkowski.training.planner.session.createDummyExercise
import com.lukasz.witkowski.training.planner.session.domain.models.SessionRpe
import com.lukasz.witkowski.training.planner.session.domain.models.SetEffort
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.session.domain.session.TrainingSessionState
import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExercise
import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExerciseConfiguration
import com.lukasz.witkowski.training.planner.session.domain.session.models.TrainingExerciseSnapshot
import com.lukasz.witkowski.training.planner.session.domain.session.queue.ExerciseQueueProgress
import com.lukasz.witkowski.training.planner.session.domain.timer.TimerState
import com.lukasz.witkowski.training.planner.session.presentation.models.TrainingSessionPhaseKey
import com.lukasz.witkowski.training.planner.session.presentation.models.TrainingSessionUiState
import com.lukasz.witkowski.training.planner.shared.utils.ResultHandler
import com.lukasz.witkowski.training.planner.training.application.TrainingPlanService
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.Date
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TrainingSessionViewModelTest {
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    private lateinit var trainingPlanService: TrainingPlanService
    private lateinit var trainingSessionService: TrainingSessionService
    private lateinit var exerciseService: ExerciseService

    private val domainStateFlow =
        MutableStateFlow<TrainingSessionState>(TrainingSessionState.IdleState)
    private val trainingPlanFlow = MutableStateFlow<TrainingPlan?>(null)
    private val trainingPlanId: TrainingPlanId = TRAINING_PLAN.id

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        trainingPlanService = mockk(relaxed = true)
        trainingSessionService = mockk(relaxed = true)
        exerciseService = mockk(relaxed = true)

        every { trainingSessionService.trainingSessionState } returns domainStateFlow
        every { trainingPlanService.getTrainingPlanById(trainingPlanId) } returns trainingPlanFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading and starts training session when TrainingPlan is emitted`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()

            assertEquals(TrainingSessionUiState.Loading, viewModel.trainingSessionState.value)
            assertEquals(
                TrainingSessionPhaseKey.Loading,
                viewModel.trainingSessionState.value.phaseKey
            )
            verify(exactly = 0) { trainingSessionService.startTraining(any(), any()) }

            // when: training plan is emitted by trainingPlanService
            trainingPlanFlow.value = TRAINING_PLAN

            // then: starts training on TrainingSessionService
            verify(exactly = 1) { trainingSessionService.startTraining(TRAINING_PLAN) }
        }

    @Test
    fun `maps ExerciseState by loading image references for exercise snapshot imageIds`() =
        runTest(testDispatcher) {
            val imageId1 = ImageId.create()
            val imageId2 = ImageId.create()
            val imageIds = listOf(imageId1, imageId2)
            val expectedReferences =
                listOf(
                    ImageReference(imageId1, "/path/1"),
                    ImageReference(imageId2, "/path/2")
                )
            val imageReferencesFlow =
                MutableStateFlow<ResultHandler<List<ImageReference>>>(ResultHandler.Loading)
            coEvery { exerciseService.loadImageReferences(imageIds) } returns imageReferencesFlow

            val exerciseId = TrainingExerciseId.create()
            val exerciseDomainState =
                createExerciseState(
                    exerciseId = exerciseId,
                    imageIds = imageIds
                )

            val viewModel = createViewModel()
            backgroundScope.launch(testDispatcher) {
                viewModel.trainingSessionState.collect()
            }

            // when: domain emits ExerciseState and image references are loading
            domainStateFlow.value = exerciseDomainState

            // then: UI state is Exercise with Loading image references and unique phaseKey
            val loadingExerciseUiState =
                viewModel.trainingSessionState.value as TrainingSessionUiState.Exercise
            assertEquals(exerciseDomainState, loadingExerciseUiState.domainState)
            assertEquals(ResultHandler.Loading, loadingExerciseUiState.imagesReferences)
            assertEquals(
                TrainingSessionPhaseKey.Exercise("Exercise_$exerciseId"),
                loadingExerciseUiState.phaseKey
            )
            coVerify(exactly = 1) { exerciseService.loadImageReferences(imageIds) }

            // when: image references finish loading
            val successResult = ResultHandler.Success(expectedReferences)
            imageReferencesFlow.value = successResult

            // then: UI state updates with loaded ImageReferences
            val loadedExerciseUiState =
                viewModel.trainingSessionState.value as TrainingSessionUiState.Exercise
            assertEquals(successResult, loadedExerciseUiState.imagesReferences)
        }

    @Test
    fun `maps RestTimeState directly to RestTime UI state`() = runTest(testDispatcher) {
        val restDomainState =
            TrainingSessionState.RestTimeState(
                nextExercise = createSessionExercise(),
                timerState = TimerState.Running(25.seconds),
                totalRestTime = 30.seconds,
                nextSetQueueProgress = null,
                selectedFeedback = SetEffort.MODERATE
            )

        val viewModel = createViewModel()
        backgroundScope.launch(testDispatcher) {
            viewModel.trainingSessionState.collect()
        }

        // when
        domainStateFlow.value = restDomainState

        // then
        val uiState = viewModel.trainingSessionState.value as TrainingSessionUiState.RestTime
        assertEquals(restDomainState, uiState.domainState)
        assertEquals(TrainingSessionPhaseKey.RestTime, uiState.phaseKey)
    }

    @Test
    fun `maps SummaryState by fetching TrainingPlan for summary statistics`() =
        runTest(testDispatcher) {
            val summaryDomainState =
                TrainingSessionState.SummaryState(
                    statistics =
                    TrainingStatistics(
                        trainingPlanId = trainingPlanId,
                        totalTime = 120.seconds,
                        date = Date(0L),
                        exercisesStatistics = emptyList(),
                        note = "Done",
                        rpe = SessionRpe.HARD
                    ),
                    selectedOverallRpe = SessionRpe.HARD,
                    notes = "Done"
                )
            val planResultFlow =
                MutableStateFlow<ResultHandler<TrainingPlan>>(ResultHandler.Loading)
            coEvery {
                trainingPlanService.fetchTrainingPlanById(trainingPlanId)
            } returns planResultFlow

            val viewModel = createViewModel()
            backgroundScope.launch(testDispatcher) {
                viewModel.trainingSessionState.collect()
            }

            // when: domain emits SummaryState
            domainStateFlow.value = summaryDomainState

            // then: fetches training plan and emits Summary UI state with Loading plan
            val loadingSummaryUiState =
                viewModel.trainingSessionState.value as TrainingSessionUiState.Summary
            assertEquals(summaryDomainState, loadingSummaryUiState.domainState)
            assertEquals(ResultHandler.Loading, loadingSummaryUiState.trainingPlan)
            assertEquals(TrainingSessionPhaseKey.Summary, loadingSummaryUiState.phaseKey)
            coVerify(exactly = 1) { trainingPlanService.fetchTrainingPlanById(trainingPlanId) }

            // when: training plan fetch succeeds
            val successPlan = ResultHandler.Success(TRAINING_PLAN)
            planResultFlow.value = successPlan

            // then: Summary UI state updates with fetched TrainingPlan
            val loadedSummaryUiState =
                viewModel.trainingSessionState.value as TrainingSessionUiState.Summary
            assertEquals(successPlan, loadedSummaryUiState.trainingPlan)
        }

    @Test
    fun `onIntent forwards all SessionIntents to TrainingSessionService`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()

            viewModel.onIntent(SessionIntent.RepsChanged(12))
            verify(exactly = 1) { trainingSessionService.changeReps(12) }

            viewModel.onIntent(SessionIntent.WeightChanged(75.5))
            verify(exactly = 1) { trainingSessionService.changeWeight(75.5) }

            viewModel.onIntent(SessionIntent.HoldTimeChanged(45.seconds))
            verify(exactly = 1) { trainingSessionService.updateHoldTime(45.seconds) }

            viewModel.onIntent(SessionIntent.StartTimer)
            verify(exactly = 1) { trainingSessionService.startTimer() }

            viewModel.onIntent(SessionIntent.ResetTimer)
            verify(exactly = 1) { trainingSessionService.resetTimer() }

            viewModel.onIntent(SessionIntent.SetAdded)
            verify(exactly = 1) { trainingSessionService.addExtraSet() }

            viewModel.onIntent(SessionIntent.SetRemoved)
            verify(exactly = 1) { trainingSessionService.removeExtraSet() }

            viewModel.onIntent(SessionIntent.SetCompleted)
            verify(exactly = 1) { trainingSessionService.completed() }

            viewModel.onIntent(SessionIntent.SetSkipped)
            verify(exactly = 1) { trainingSessionService.skip() }

            viewModel.onIntent(SessionIntent.RestSkipped)
            verify(exactly = 2) { trainingSessionService.skip() }

            viewModel.onIntent(SessionIntent.SetFeedbackSelected(SetEffort.HARD))
            verify(exactly = 1) { trainingSessionService.setExerciseSetFeedback(SetEffort.HARD) }

            viewModel.onIntent(SessionIntent.SessionNotesChanged("Strong finish"))
            verify(exactly = 1) { trainingSessionService.updateSessionNotes("Strong finish") }

            viewModel.onIntent(SessionIntent.SessionFeedbackSelected(SessionRpe.VERY_HARD))
            verify(exactly = 1) {
                trainingSessionService.updateSessionFeedback(SessionRpe.VERY_HARD)
            }

            viewModel.onIntent(SessionIntent.SessionEndEarly)
            verify(exactly = 1) { trainingSessionService.endEarly() }
        }

    private fun createViewModel() = TrainingSessionViewModel(
        trainingPlanService = trainingPlanService,
        trainingSessionService = trainingSessionService,
        exerciseService = exerciseService,
        trainingPlanId = trainingPlanId
    )

    private fun createExerciseState(
        exerciseId: TrainingExerciseId = TrainingExerciseId.create(),
        imageIds: List<ImageId> = emptyList()
    ): TrainingSessionState.ExerciseState = TrainingSessionState.ExerciseState(
        sessionExercise = createSessionExercise(exerciseId = exerciseId, imageIds = imageIds),
        timerState = TimerState.NotSet,
        progress =
        ExerciseQueueProgress(
            remainingExerciseSets = 1,
            currentExerciseTotalSets = 2,
            totalExercises = 2,
            remainingExercises = 2,
            remainingSetsInWorkout = 3,
            nextSetQueueProgress = null
        )
    )

    private fun createSessionExercise(
        exerciseId: TrainingExerciseId = TrainingExerciseId.create(),
        imageIds: List<ImageId> = emptyList()
    ): SessionExercise {
        val config =
            SessionExerciseConfiguration(
                reps = 10,
                weightInKg = 50.0,
                holdTime = null
            )
        val dummy = createDummyExercise().copy(imageIds = imageIds)
        val trainingExercise =
            TrainingExercise(
                id = exerciseId,
                exercise = dummy,
                sets = 2,
                repetitions = 10,
                restTime = 30.seconds
            )
        return SessionExercise(
            snapshot =
            TrainingExerciseSnapshot(
                id = trainingExercise.id,
                name = dummy.name,
                description = dummy.description,
                categories = dummy.categories,
                imageIds = imageIds,
                plannedConfig = config
            ),
            currentConfig = config,
            restTime = 30.seconds
        )
    }
}
