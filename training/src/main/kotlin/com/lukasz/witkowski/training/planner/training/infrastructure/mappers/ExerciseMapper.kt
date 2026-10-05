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
            trainingExerciseId = this.id,
            trainingPlanId = trainingPlanId
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
    restTime = restTime.inWholeSeconds,
    weightInKg = weightInKg
)

private fun ExerciseCategoryName.toDbExerciseCategory(
    trainingExerciseId: TrainingExerciseId,
    trainingPlanId: TrainingPlanId,
) =
    DbExerciseCategory(
        trainingExerciseId = trainingExerciseId.toString(),
        trainingPlanId = trainingPlanId.toString(),
        name = this.name
    )


internal fun DbTrainingExerciseWithCategories.toTrainingExercise() =
    TrainingExercise(
        id = TrainingExerciseId(trainingExercise.id),
        exercise = trainingExercise.toExerciseSnapshot(
            categories = this.categories
        ),
        repetitions = trainingExercise.repetitions,
        sets = trainingExercise.sets,
        weightInKg = trainingExercise.weightInKg,
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
