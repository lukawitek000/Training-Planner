package com.lukasz.witkowski.training.planner.training.infrastructure.mappers

import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseId
import com.lukasz.witkowski.training.planner.training.domain.ExerciseCategoryName
import com.lukasz.witkowski.training.planner.training.domain.ExerciseSnapshot
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanConfiguration
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbExerciseCategory
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbTrainingExercise
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbTrainingExerciseWithCategories
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbTrainingPlan
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbTrainingPlanWithExercises
import java.util.UUID
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

internal fun TrainingPlanConfiguration.toDbTrainingPlanWithExercises(
    id: TrainingPlanId,
    currentInstant: Instant,
): DbTrainingPlanWithExercises {
    return DbTrainingPlanWithExercises(
        trainingPlan = this.toDbTrainingPlan(id, currentInstant),
        exercises = this.exercises.mapIndexed { index, trainingExercise ->
            trainingExercise.toDbTrainingExerciseWithCategories(
                trainingPlanId = id,
                position = index
            )
        }
    )
}

private fun TrainingPlanConfiguration.toDbTrainingPlan(
    id: TrainingPlanId,
    currentInstant: Instant
) =
    DbTrainingPlan(
        id = id.toString(),
        name = title,
        description = description,
        restTime = restTime.inWholeSeconds,
        lastModified = currentInstant,
        lastUsed = null
    )

private fun TrainingExercise.toDbTrainingExerciseWithCategories(
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


internal fun DbTrainingPlanWithExercises.toTrainingPlan() =
    TrainingPlan(
        id = TrainingPlanId(UUID.fromString(trainingPlan.id)),
        title = trainingPlan.name,
        description = trainingPlan.description,
        exercises = exercises.map {
            it.toTrainingExercise()
        },
        restTime = trainingPlan.restTime.seconds,
        lastModification = trainingPlan.lastModified,
        lastSession = trainingPlan.lastUsed
    )

private fun DbTrainingExerciseWithCategories.toTrainingExercise() =
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

private fun DbExerciseCategory.toExerciseCategoryName() = ExerciseCategoryName(name)
