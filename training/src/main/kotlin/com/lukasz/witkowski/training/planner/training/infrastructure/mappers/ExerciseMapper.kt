package com.lukasz.witkowski.training.planner.training.infrastructure.mappers

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.training.domain.ExerciseCategoryName
import com.lukasz.witkowski.training.planner.training.domain.ExerciseSnapshot
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbExerciseCategory
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbTrainingExercise
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbTrainingExerciseWithCategories
import kotlin.time.Duration.Companion.seconds


internal fun TrainingExercise.toDbTrainingExerciseWithCategories(
    trainingPlanId: TrainingPlanId,
    position: Int,
) = DbTrainingExerciseWithCategories(
    trainingExercise = this.toDbTrainingExercise(trainingPlanId, position),
    categories = this.exercise.categories.map {
        it.toDbExerciseCategory(
            exerciseId = exercise.id,
            trainingPlanId = trainingPlanId,
            position = position
        )
    }
)

private fun TrainingExercise.toDbTrainingExercise(
    trainingPlanId: TrainingPlanId,
    position: Int,
) = DbTrainingExercise(
    id = id.toString(),
    trainingId = trainingPlanId.toString(),
    exerciseId = exercise.id.toString(),
    position = position,
    name = exercise.name,
    description = exercise.description,
    imagePath = null,
    repetitions = repetitions,
    sets = sets,
    restTime = restTime.inWholeSeconds
)

internal fun ExerciseCategoryName.toDbExerciseCategory(
    exerciseId: ExerciseId,
    trainingPlanId: TrainingPlanId,
    position: Int
) =
    DbExerciseCategory(
        exerciseId = exerciseId.toString(),
        trainingPlanId = trainingPlanId.toString(),
        position = position,
        name = this.name
    )


internal fun DbTrainingExerciseWithCategories.toTrainingExercise() =
    TrainingExercise(
        id = TrainingExerciseId.create(),
        exercise = trainingExercise.toExerciseSnapshot(
            categories = this.categories
        ),
        repetitions = trainingExercise.repetitions,
        sets = trainingExercise.sets,
        restTime = trainingExercise.restTime.seconds,
    )

private fun DbTrainingExercise.toExerciseSnapshot(
    categories: List<DbExerciseCategory>
) =
    ExerciseSnapshot(
        id = ExerciseId(this.exerciseId),
        name = name,
        description = description,
        imageId = null,
        categories = categories.map { it.toExerciseCategoryName() }.toSet()
    )

internal fun DbExerciseCategory.toExerciseCategoryName() = ExerciseCategoryName(name)
