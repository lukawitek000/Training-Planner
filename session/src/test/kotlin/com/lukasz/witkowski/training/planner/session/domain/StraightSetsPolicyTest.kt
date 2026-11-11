package com.lukasz.witkowski.training.planner.session.domain

import com.lukasz.witkowski.training.planner.session.createDummyExercise
import com.lukasz.witkowski.training.planner.session.domain.session.StraightSetsPolicy
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class StraightSetsPolicyTest {
    private lateinit var straightSetsPolicy: StraightSetsPolicy

    @BeforeTest
    fun setUp() {
        straightSetsPolicy = StraightSetsPolicy()
    }

    @Test
    fun `load exercises in straight sets sequence with exercise and plan rest times`() {
        // given
        val exercise1 =
            TrainingExercise(
                id = TrainingExerciseId.create(),
                sets = 3,
                restTime = 30.seconds,
                exercise = createDummyExercise("Bench Press")
            )
        val exercise2 =
            TrainingExercise(
                id = TrainingExerciseId.create(),
                sets = 2,
                restTime = 45.seconds,
                exercise = createDummyExercise("Incline Fly")
            )
        val trainingPlan = createTrainingPlan(listOf(exercise1, exercise2), restTime = 90.seconds)

        // when
        val loadedExercises = straightSetsPolicy.loadExercises(trainingPlan)

        // then: all sets of exercise 1, then all sets of exercise 2
        val expectedIds =
            listOf(
                exercise1.id,
                exercise1.id,
                exercise1.id,
                exercise2.id,
                exercise2.id
            )
        assertContentEquals(expectedIds, loadedExercises.map { it.exerciseId })

        // Intermediate sets use exercise.restTime; final set of each exercise uses trainingPlan.restTime
        assertEquals(
            listOf(30.seconds, 30.seconds, 90.seconds, 45.seconds, 90.seconds),
            loadedExercises.map { it.restTime }
        )
    }

    @Test
    fun `load empty plan returns empty list`() {
        val trainingPlan = createTrainingPlan(emptyList())
        val loadedExercises = straightSetsPolicy.loadExercises(trainingPlan)
        assertContentEquals(emptyList(), loadedExercises)
    }

    private fun createTrainingPlan(
        trainingExercises: List<TrainingExercise>,
        restTime: kotlin.time.Duration = 60.seconds
    ): TrainingPlan = TrainingPlan(
        id = TrainingPlanId.create(),
        title = "Training Plan",
        description = "Description",
        exercises = trainingExercises,
        restTime = restTime,
        lastModification = Instant.parse("2026-09-04T12:00:00Z"),
        lastSession = null
    )
}
