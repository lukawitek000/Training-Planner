package com.lukasz.witkowski.training.planner

import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category
import com.lukasz.witkowski.training.planner.exercise.presentation.models.Exercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingExercise
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingPlan

val dummyTrainingsList = listOf<TrainingPlan>(
    TrainingPlan(
        id = TrainingPlanId.create(),
        title = "Training 1",
        description = "Some descritpion of the training 1",
        listOf(
            TrainingExercise(
                TrainingExerciseId.create(),
                Exercise(
                    ExerciseId.create(),
                    "Exercise 1.1",
                    "Description 1.2",
                    Category(2, R.string.category_back),
                    null
                ),
                repetitions = 10, sets = 3, restTime = Time(minutes = 1, seconds = 30),
                time = Time(minutes = 0, seconds = 5)
            ),
            TrainingExercise(
                TrainingExerciseId.create(),
                Exercise(
                    ExerciseId.create(),
                    "Exercise 1.2",
                    "Description 1.2",
                    Category(3, R.string.category_abs),
                    null
                ),
                repetitions = 20,
                sets = 4,
                restTime = Time(minutes = 0, seconds = 10),
            ),
            TrainingExercise(
                TrainingExerciseId.create(),
                Exercise(
                    ExerciseId.create(),
                    "Exercise 1.3",
                    "Description 1.3",
                    Category(4, R.string.category_biceps),
                    null
                ),
                repetitions = 10, sets = 3, restTime = Time(minutes = 0, seconds = 30)
            ),
            TrainingExercise(
                TrainingExerciseId.create(),
                Exercise(
                    ExerciseId.create(),
                    "Exercise 1.4",
                    "Description 1.4",
                    Category(2, R.string.category_back),
                    null
                ),
                repetitions = 40,
                sets = 2,
                restTime = Time(minutes = 0, seconds = 15),
                time = Time(seconds = 30)
            ),
        )
    ),
    TrainingPlan(
        id = TrainingPlanId.create(),
        title = "Training 2",
        description = "Some descritpion of the training 2",
        listOf(
            TrainingExercise(
                TrainingExerciseId.create(),
                Exercise(ExerciseId.create(), "Exercise 2.1", "Description 2.2", Category(0), null),
                repetitions = 10, sets = 3, restTime = Time(minutes = 1, seconds = 30)
            ),
            TrainingExercise(
                TrainingExerciseId.create(),
                Exercise(ExerciseId.create(), "Exercise 2.2", "Description 2.2", Category(0), null),
                repetitions = 10, sets = 3, time = Time(seconds = 30)
            ),
            TrainingExercise(
                TrainingExerciseId.create(),
                Exercise(ExerciseId.create(), "Exercise 2.3", "Description 2.3", Category(0), null),
                repetitions = 10, sets = 3, restTime = Time(minutes = 1, seconds = 30)
            )
        )
    )
)