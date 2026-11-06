package com.lukasz.witkowski.training.planner.session.domain

import com.lukasz.witkowski.training.planner.session.createDummyExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class CircuitSetsPolicyTest {
    private lateinit var circuitSetsPolicy: CircuitSetsPolicy

    @BeforeTest
    fun setUp() {
        circuitSetsPolicy = CircuitSetsPolicy()
    }

    @Test
    fun `load two exercises with different number of sets`() {
        // given
        val exercises = create2Exercises()
        val trainingPlan = createTrainingPlan(exercises)

        // when
        val loadedExercises = circuitSetsPolicy.loadExercises(trainingPlan)

        // then
        val expectedExercisesOrder =
            listOf(
                exercises.first(),
                exercises[1],
                exercises.first(),
                exercises[1],
                exercises[1],
                exercises[1]
            )
        assertContentEquals(expectedExercisesOrder, loadedExercises)
    }

    @Test
    fun `load single exercise with many sets`() {
        // given
        val exercises =
            listOf(
                TrainingExercise(
                    id = TrainingExerciseId.create(),
                    sets = 3,
                    exercise = createDummyExercise("Push-up")
                )
            )
        val trainingPlan = createTrainingPlan(exercises)

        // when
        val loadedExercises = circuitSetsPolicy.loadExercises(trainingPlan)

        // then
        val expectedExercisesOrder = listOf(exercises.first(), exercises.first(), exercises.first())
        assertContentEquals(expectedExercisesOrder, loadedExercises)
    }

    @Test
    fun `load empty exercise list`() {
        // given
        val exercises = emptyList<TrainingExercise>()
        val trainingPlan = createTrainingPlan(exercises)

        // when
        val loadedExercises = circuitSetsPolicy.loadExercises(trainingPlan)

        // then
        assertContentEquals(emptyList(), loadedExercises)
    }

    @Test
    fun `load few exercises with the same sets`() {
        // given
        val exercises = create4Exercises()
        val trainingPlan = createTrainingPlan(exercises)

        // when
        val loadedExercises = circuitSetsPolicy.loadExercises(trainingPlan)

        // then
        val expectedExercisesOrder =
            listOf(
                exercises[0],
                exercises[1],
                exercises[2],
                exercises[3],
                exercises[0],
                exercises[1],
                exercises[2],
                exercises[3]
            )
        assertContentEquals(expectedExercisesOrder, loadedExercises)
    }

    @Suppress("LongMethod")
    private fun create4Exercises(): List<TrainingExercise> {
        val exercise1 =
            TrainingExercise(
                id = TrainingExerciseId.create(),
                sets = 2,
                exercise = createDummyExercise("Exercise 1")
            )
        val exercise2 =
            TrainingExercise(
                id = TrainingExerciseId.create(),
                sets = 2,
                exercise = createDummyExercise("Exercise 2")
            )
        val exercise3 =
            TrainingExercise(
                id = TrainingExerciseId.create(),
                sets = 2,
                exercise = createDummyExercise("Exercise 3")
            )
        val exercise4 =
            TrainingExercise(
                id = TrainingExerciseId.create(),
                sets = 2,
                exercise = createDummyExercise("Exercise 4")
            )
        return listOf(exercise1, exercise2, exercise3, exercise4)
    }

    private fun create2Exercises(): List<TrainingExercise> {
        val exercise1 =
            TrainingExercise(
                id = TrainingExerciseId.create(),
                sets = 2,
                exercise = createDummyExercise("Exercise 1")
            )
        val exercise2 =
            TrainingExercise(
                id = TrainingExerciseId.create(),
                sets = 4,
                exercise = createDummyExercise("Exercise 2")
            )
        return listOf(exercise1, exercise2)
    }

    private fun createTrainingPlan(trainingExercises: List<TrainingExercise>): TrainingPlan =
        TrainingPlan(
            id = TrainingPlanId.create(),
            title = "Training Plan Title",
            description = "Test description",
            exercises = trainingExercises,
            restTime = 60.seconds,
            lastModification = Instant.parse("2026-09-04T12:00:00Z"),
            lastSession = null
        )
}
