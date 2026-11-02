package com.lukasz.witkowski.training.planner.training.presentation.editor

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.training.domain.ExerciseCategoryName
import com.lukasz.witkowski.training.planner.training.domain.ExerciseSnapshot
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanConfiguration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class TrainingConfigurationReducerTest {
    private val reducer = TrainingConfigurationReducer()

    private val initialConfig = TrainingPlanConfiguration(
        title = "Initial Title",
        description = "Initial Description",
        exercises = emptyList(),
        restTime = 60.seconds
    )

    private val sampleExercise = TrainingExercise(
        id = TrainingExerciseId.create(),
        exercise = ExerciseSnapshot(
            id = ExerciseId.create(),
            name = "Push ups",
            description = "Chest",
            categories = setOf(ExerciseCategoryName("Chest"))
        ),
        repetitions = 10,
        sets = 3,
        restTime = 30.seconds
    )

    @Test
    fun `title changed updates title`() {
        val result = reducer.reduce(
            initialConfig,
            TrainingPlanEditingIntent.TitleChanged("New Title")
        )
        assertEquals("New Title", result.title)
    }

    @Test
    fun `description changed updates description`() {
        val result = reducer.reduce(
            initialConfig,
            TrainingPlanEditingIntent.DescriptionChanged("New Description")
        )
        assertEquals("New Description", result.description)
    }

    @Test
    fun `rest time changed updates rest time`() {
        val result = reducer.reduce(
            initialConfig,
            TrainingPlanEditingIntent.RestTimeChanged(120.seconds)
        )
        assertEquals(120.seconds, result.restTime)
    }

    @Test
    fun `exercise added and removed updates exercises list`() {
        val withExercise = reducer.reduce(
            initialConfig,
            TrainingPlanEditingIntent.TrainingExerciseAdded(sampleExercise)
        )
        assertEquals(listOf(sampleExercise), withExercise.exercises)

        val removed = reducer.reduce(
            withExercise,
            TrainingPlanEditingIntent.TrainingExerciseRemoved(sampleExercise)
        )
        assertEquals(emptyList(), removed.exercises)
    }

    @Test
    fun `exercises reordered modifies positions`() {
        val exercise1 = sampleExercise
        val exercise2 = sampleExercise.copy(id = TrainingExerciseId.create())
        val configWithTwo = initialConfig.copy(exercises = listOf(exercise1, exercise2))

        val reordered = reducer.reduce(
            configWithTwo,
            TrainingPlanEditingIntent.TrainingExercisesReordered(0, 1)
        )
        assertEquals(listOf(exercise2, exercise1), reordered.exercises)
    }
}
