package com.lukasz.witkowski.training.planner

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
                TrainingExerciseId.create(), Exercise(ExerciseId.create(), "Exercise 1.1", "Description 1.2", Category(2, R.string.category_back), null)
            ),
            TrainingExercise(
                TrainingExerciseId.create(), Exercise(ExerciseId.create(), "Exercise 1.2", "Description 1.2", Category(2, R.string.category_abs), null)
            ),
            TrainingExercise(
                TrainingExerciseId.create(), Exercise(ExerciseId.create(), "Exercise 1.3", "Description 1.3", Category(2, R.string.category_biceps), null)
            ),
            TrainingExercise(
                TrainingExerciseId.create(),
                Exercise(ExerciseId.create(), "Exercise 1.4", "Description 1.4", Category(2, R.string.category_back), null)
            ),
        )
    ),
    TrainingPlan(
        id = TrainingPlanId.create(),
        title = "Training 2",
        description = "Some descritpion of the training 2",
        listOf(
            TrainingExercise(
                TrainingExerciseId.create(), Exercise(ExerciseId.create(), "Exercise 2.1", "Description 2.2", Category(2, R.string.category_none), null)
            ),
            TrainingExercise(
                TrainingExerciseId.create(), Exercise(ExerciseId.create(), "Exercise 2.2", "Description 2.2", Category(2, R.string.category_none), null)
            ),
            TrainingExercise(
                TrainingExerciseId.create(), Exercise(ExerciseId.create(), "Exercise 2.3", "Description 2.3", Category(2, R.string.category_none), null)
            ),
            TrainingExercise(
                TrainingExerciseId.create(), Exercise(ExerciseId.create(), "Exercise 2.4", "Description 2.4", Category(2, R.string.category_none), null)
            ),
        )
    )
)