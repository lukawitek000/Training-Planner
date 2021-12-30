package com.lukasz.witkowski.training.wearable

import com.lukasz.witkowski.shared.models.Category
import com.lukasz.witkowski.shared.models.Exercise
import com.lukasz.witkowski.shared.models.Training
import com.lukasz.witkowski.shared.models.TrainingExercise
import com.lukasz.witkowski.shared.models.TrainingWithExercises

val trainingsList = listOf<TrainingWithExercises>(
    TrainingWithExercises(
        Training(1L, "Training 1", "Some description of training 1"),
        listOf(
            TrainingExercise(
                1L, 1L,
                Exercise(0L, "Name exercise 1", "Exercise description 1", Category.Biceps),
                10, 3, 0L
            ),
            TrainingExercise(
                2L, 1L,
                Exercise(0L, "Name exercise 2", "Exercise description 2", Category.Abs),
                15, 3, 600L
            ),
            TrainingExercise(
                3L, 1L,
                Exercise(0L, "Name exercise 3", "Exercise description 3", Category.Cardio),
                10, 3, 1000L
            ),
            TrainingExercise(
                4L, 1L,
                Exercise(0L, "Name exercise 4", "Exercise description 4", Category.Legs),
                100, 1, 0L
            ),
            TrainingExercise(
                5L, 1L,
                Exercise(0L, "Name exercise 5", "Exercise description 5", Category.Biceps),
                10, 3, 0L
            )
        )
    ),
    TrainingWithExercises(
        Training(2L, "Training 2", "Some description of training 2"),
        listOf(
            TrainingExercise(
                0L, 1L,
                Exercise(0L, "Name exercise 21", "Exercise description 21", Category.Biceps),
                10, 3, 0L
            ),
            TrainingExercise(
                0L, 1L,
                Exercise(0L, "Name exercise 22", "Exercise description 22", Category.Abs),
                15, 3, 600L
            ),
            TrainingExercise(
                0L, 1L,
                Exercise(0L, "Name exercise 23", "Exercise description 23", Category.Cardio),
                10, 3, 1000L
            ),
            TrainingExercise(
                0L, 1L,
                Exercise(0L, "Name exercise2 4", "Exercise description 24", Category.Legs),
                100, 1, 0L
            ),
            TrainingExercise(
                0L, 1L,
                Exercise(0L, "Name exercise 25", "Exercise description 25", Category.Biceps),
                130, 3, 0L
            )
        )
    ),
    TrainingWithExercises(
        Training(3L, "Training 31", "Some description of training 31"),
        listOf(
            TrainingExercise(
                13L, 3L,
                Exercise(0L, "Name exercise 31", "Exercise description 31", Category.Biceps),
                10, 3, 0L, restTime = 5000L
            ),
            TrainingExercise(
                23L, 3L,
                Exercise(0L, "Name exercise 32", "Exercise description 32", Category.Abs),
                15, 2, 6000L
            ),
        )
    ),
    TrainingWithExercises(
        Training(4L, "Training 4", "Some description of training 4"),
        listOf(
            TrainingExercise(
                0L, 1L,
                Exercise(0L, "Name exercise 41", "Exercise description4 1", Category.Biceps),
                10, 3, 0L,10000L
            ),
            TrainingExercise(
                0L, 1L,
                Exercise(0L, "Name exercise 42", "Exercise description4 2", Category.Abs),
                15, 3, 600L,200L
            ),
            TrainingExercise(
                0L, 1L,
                Exercise(0L, "Name exercise 43", "Exercise description 43", Category.Cardio),
                10, 3, 1000L,30000L
            ),
            TrainingExercise(
                0L, 1L,
                Exercise(0L, "Name exercise 44", "Exercise description4 4", Category.Legs),
                100, 1, 0L,60000L
            ),
            TrainingExercise(
                0L, 1L,
                Exercise(0L, "Name exercise4 5", "Exercise description 45", Category.Biceps),
                10, 3, 0L,10000L
            )
        )
    ),
    TrainingWithExercises(
        Training(5L, "Training 5", "Some description of training 5"),
        listOf(
            TrainingExercise(
                0L, 1L,
                Exercise(0L, "Name exercise5 1", "Exercise description 51", Category.Biceps),
                10, 2, 0L, 6000L
            ),
            TrainingExercise(
                0L, 1L,
                Exercise(0L, "Name exercise 52", "Exercise description 52", Category.Abs),
                15, 3, 60000L, 0L
            ),
            TrainingExercise(
                0L, 1L,
                Exercise(0L, "Name exercise 53", "Exercise description 53", Category.Cardio),
                10, 2, 10000L, 60000L
            ),
            TrainingExercise(
                0L, 1L,
                Exercise(0L, "Name exercise 54", "Exercise description 54", Category.Legs),
                100, 1, 5000L, 20000L
            ),
            TrainingExercise(
                0L, 1L,
                Exercise(0L, "Name exercise 55", "Exercise description 55", Category.Biceps),
                10, 3, 5000L, 0L
            )
        )
    )


)