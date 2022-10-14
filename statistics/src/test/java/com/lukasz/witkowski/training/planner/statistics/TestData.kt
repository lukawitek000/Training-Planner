package com.lukasz.witkowski.training.planner.statistics

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId


val TRAINING_EXERCISES = createTrainingExercisesWithDifferentSets()
val TRAINING_PLAN = createTrainingPlan(TRAINING_EXERCISES)

fun createTrainingPlan(trainingExercises: List<TrainingExercise>): TrainingPlan {
    return TrainingPlan(
        id = TrainingPlanId.create(),
        title = "Training Plan Title",
        exercises = trainingExercises
    )
}

private fun createTrainingExercisesWithDifferentSets(): List<TrainingExercise> {
    val exercise1 = TrainingExercise(
        id = TrainingExerciseId.create(),
        repetitions = 10,
        sets = 2,
        time = Time(10000L),
        restTime = Time(30000)
    )
    val exercise2 = TrainingExercise(
        id = TrainingExerciseId.create(),
        repetitions = 10,
        sets = 3,
        time = Time(10000L),
        restTime = Time.NONE
    )
    val exercise3 = TrainingExercise(
        id = TrainingExerciseId.create(),
        repetitions = 10,
        sets = 1,
        time = Time(10000L),
        restTime = Time(30000)
    )
    val exercise4 = TrainingExercise(
        id = TrainingExerciseId.create(),
        repetitions = 10,
        sets = 5,
        time = Time(10000L),
        restTime = Time(30000)
    )
    return listOf(exercise1, exercise2, exercise3, exercise4)
}