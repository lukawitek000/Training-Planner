package com.lukasz.witkowski.training.planner

import com.lukasz.witkowski.training.planner.exercise.presentation.models.Category
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.presentation.TrainingExercise
import com.lukasz.witkowski.training.planner.training.presentation.TrainingPlan

val dummyTrainingsList = listOf<TrainingPlan>(
    TrainingPlan(
        id = TrainingPlanId.create(),
        title = "Training 1",
        description = "Some descritpion of the training 1",
        listOf(
            TrainingExercise(
                TrainingExerciseId.create(), "Exercise 1.1", "Description 1.2", Category(2, R.string.category_back)
            ),
            TrainingExercise(
                TrainingExerciseId.create(), "Exercise 1.2", "Description 1.2", Category(2, R.string.category_abs)
            ),TrainingExercise(
                TrainingExerciseId.create(), "Exercise 1.3", "Description 1.3", Category(2, R.string.category_biceps)
            ),
            TrainingExercise(
                TrainingExerciseId.create(), "Exercise 1.4", "Description 1.4", Category(2, R.string.category_back)
            ),
        )
    ),
    TrainingPlan(
        id = TrainingPlanId.create(),
        title = "Training 2",
        description = "Some descritpion of the training 2",
        listOf(
            TrainingExercise(
                TrainingExerciseId.create(), "Exercise 2.1", "Description 2.2"
            ),
            TrainingExercise(
                TrainingExerciseId.create(), "Exercise 2.2", "Description 2.2"
            ),TrainingExercise(
                TrainingExerciseId.create(), "Exercise 2.3", "Description 2.3"
            ),
            TrainingExercise(
                TrainingExerciseId.create(), "Exercise 2.4", "Description 2.4"
            ),
        )
    ),TrainingPlan(
        id = TrainingPlanId.create(),
        title = "Training 3",
        description = "Some descritpion of the training 3",
        listOf(
            TrainingExercise(
                TrainingExerciseId.create(), "Exercise 3.1", "Description 3.2"
            ),
            TrainingExercise(
                TrainingExerciseId.create(), "Exercise 3.2", "Description 3.2"
            ),TrainingExercise(
                TrainingExerciseId.create(), "Exercise 3.3", "Description 3.3"
            ),
            TrainingExercise(
                TrainingExerciseId.create(), "Exercise 3.4", "Description 3.4"
            ),
        )
    ),TrainingPlan(
        id = TrainingPlanId.create(),
        title = "Training 4",
        description = "Some descritpion of the training 4",
        listOf(
            TrainingExercise(
                TrainingExerciseId.create(), "Exercise 4.1", "Description 4.2"
            ),
            TrainingExercise(
                TrainingExerciseId.create(), "Exercise 4.2", "Description 4.2"
            ),TrainingExercise(
                TrainingExerciseId.create(), "Exercise 4.3", "Description 4.3"
            ),
            TrainingExercise(
                TrainingExerciseId.create(), "Exercise 4.4", "Description 4.4"
            ),
        )
    )

)