package com.lukasz.witkowski.training.planner.session

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.training.domain.ExerciseCategoryName
import com.lukasz.witkowski.training.planner.training.domain.ExerciseSnapshot
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

val TRAINING_EXERCISES = createTrainingExercisesWithDifferentSets()
val TRAINING_PLAN = createTrainingPlan(TRAINING_EXERCISES)

fun createTrainingPlan(
    trainingExercises: List<TrainingExercise>,
    restTime: Duration = 30.seconds
): TrainingPlan = TrainingPlan(
    id = TrainingPlanId.create(),
    title = "Training Plan Title",
    description = "Test plan description",
    exercises = trainingExercises,
    restTime = restTime,
    lastModification = Instant.parse("2026-09-04T12:00:00Z"),
    lastSession = null
)

@Suppress("LongMethod")
private fun createTrainingExercisesWithDifferentSets(): List<TrainingExercise> {
    val exercise1 =
        TrainingExercise(
            id = TrainingExerciseId.create(),
            repetitions = 10,
            sets = 2,
            restTime = 30.seconds,
            exercise = createDummyExercise("Exercise 1")
        )
    val exercise2 =
        TrainingExercise(
            id = TrainingExerciseId.create(),
            repetitions = 10,
            sets = 3,
            restTime = Duration.ZERO,
            exercise = createDummyExercise("Exercise 2")
        )
    val exercise3 =
        TrainingExercise(
            id = TrainingExerciseId.create(),
            repetitions = 10,
            sets = 1,
            restTime = 30.seconds,
            exercise = createDummyExercise("Exercise 3")
        )
    val exercise4 =
        TrainingExercise(
            id = TrainingExerciseId.create(),
            repetitions = 10,
            sets = 5,
            restTime = 30.seconds,
            exercise = createDummyExercise("Exercise 4")
        )
    return listOf(exercise1, exercise2, exercise3, exercise4)
}

fun createDummyExercise(name: String = "Test Exercise") = ExerciseSnapshot(
    id = ExerciseId.create(),
    name = name,
    description = "Test exercise description",
    categories = setOf(ExerciseCategoryName("Legs")),
    imageId = null
)

fun createTrainingExercise(
    name: String = "Test Exercise",
    sets: Int = 3,
    repetitions: Int = 10,
    restTime: Duration = 30.seconds
) = TrainingExercise(
    id = TrainingExerciseId.create(),
    exercise = createDummyExercise(name),
    sets = sets,
    repetitions = repetitions,
    restTime = restTime
)
