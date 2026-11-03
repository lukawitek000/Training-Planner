package com.lukasz.witkowski.training.planner.session.domain

import com.lukasz.witkowski.training.planner.session.createDummyExercise
import com.lukasz.witkowski.training.planner.session.domain.session.StraightSetsPolicy
import com.lukasz.witkowski.training.planner.session.domain.session.SupersetSetsPolicy
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class StraightSetsPolicyTest {
    private lateinit var straightSetsPolicy: StraightSetsPolicy

    @BeforeTest
    fun setUp() {
        straightSetsPolicy = StraightSetsPolicy()
    }

    @Test
    fun `load exercises in straight sets sequence`() {
        // given
        val exercise1 = TrainingExercise(
            id = TrainingExerciseId.create(),
            sets = 3,
            exercise = createDummyExercise("Bench Press")
        )
        val exercise2 = TrainingExercise(
            id = TrainingExerciseId.create(),
            sets = 2,
            exercise = createDummyExercise("Incline Fly")
        )
        val trainingPlan = createTrainingPlan(listOf(exercise1, exercise2))

        // when
        val loadedExercises = straightSetsPolicy.loadExercises(trainingPlan)

        // then: all sets of exercise 1, then all sets of exercise 2
        val expectedOrder = listOf(
            exercise1,
            exercise1,
            exercise1,
            exercise2,
            exercise2
        )
        assertContentEquals(expectedOrder, loadedExercises)
    }

    @Test
    fun `load empty plan returns empty list`() {
        val trainingPlan = createTrainingPlan(emptyList())
        val loadedExercises = straightSetsPolicy.loadExercises(trainingPlan)
        assertContentEquals(emptyList(), loadedExercises)
    }

    @Test
    fun `superset policy alternates between pairs`() {
        val supersetPolicy = SupersetSetsPolicy()
        val exerciseA = TrainingExercise(
            id = TrainingExerciseId.create(),
            sets = 2,
            exercise = createDummyExercise("Exercise A")
        )
        val exerciseB = TrainingExercise(
            id = TrainingExerciseId.create(),
            sets = 2,
            exercise = createDummyExercise("Exercise B")
        )
        val trainingPlan = createTrainingPlan(listOf(exerciseA, exerciseB))

        val loadedExercises = supersetPolicy.loadExercises(trainingPlan)

        val expectedOrder = listOf(
            exerciseA,
            exerciseB,
            exerciseA,
            exerciseB
        )
        assertContentEquals(expectedOrder, loadedExercises)
    }

    private fun createTrainingPlan(trainingExercises: List<TrainingExercise>): TrainingPlan =
        TrainingPlan(
            id = TrainingPlanId.create(),
            title = "Training Plan",
            description = "Description",
            exercises = trainingExercises,
            restTime = 60.seconds,
            lastModification = Instant.parse("2026-09-04T12:00:00Z"),
            lastSession = null
        )
}
