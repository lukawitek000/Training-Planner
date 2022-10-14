package com.lukasz.witkowski.training.planner.statistics.domain

import com.lukasz.witkowski.training.planner.exercise.domain.Exercise
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.statistics.createDummyExercise
import com.lukasz.witkowski.training.planner.statistics.domain.session.CircuitSetsPolicy
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import org.junit.Before
import org.junit.Test
import kotlin.test.assertContentEquals

class CircuitSetsPolicyTest {

    private lateinit var circuitSetsPolicy: CircuitSetsPolicy

    @Before
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
        val expectedExercisesOrder = listOf(
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
        val exercises = listOf(TrainingExercise(id = TrainingExerciseId.create(), sets = 3, exercise = createDummyExercise()))
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
        val expectedExercisesOrder = listOf(
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

    private fun create4Exercises(): List<TrainingExercise> {
        val exercise1 = TrainingExercise(
            id = TrainingExerciseId.create(),
            sets = 2,
            exercise = createDummyExercise()
        )
        val exercise2 = TrainingExercise(
            id = TrainingExerciseId.create(),
            sets = 2,
            exercise = createDummyExercise()
        )
        val exercise3 = TrainingExercise(
            id = TrainingExerciseId.create(),
            sets = 2,
            exercise = createDummyExercise()
        )
        val exercise4 = TrainingExercise(
            id = TrainingExerciseId.create(),
            sets = 2,
            exercise = createDummyExercise()
        )
        return listOf(exercise1, exercise2, exercise3, exercise4)
    }

    private fun create2Exercises(): List<TrainingExercise> {
        val exercise1 = TrainingExercise(
            id = TrainingExerciseId.create(),
            sets = 2,
            exercise = createDummyExercise()
        )
        val exercise2 = TrainingExercise(
            id = TrainingExerciseId.create(),
            sets = 4,
            exercise = createDummyExercise()
        )
        return listOf(exercise1, exercise2)
    }

    private fun createTrainingPlan(trainingExercises: List<TrainingExercise>): TrainingPlan {
        return TrainingPlan(
            id = TrainingPlanId.create(),
            title = "Training Plan Title",
            exercises = trainingExercises
        )
    }
}