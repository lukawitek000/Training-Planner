package com.lukasz.witkowski.training.planner.training.infrastructure.mappers

import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanConfiguration
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanOverview
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbTrainingOverview
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbTrainingPlan
import com.lukasz.witkowski.training.planner.training.infrastructure.models.DbTrainingPlanWithExercises
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

internal fun TrainingPlanConfiguration.toDbTrainingPlanWithExercises(
    id: TrainingPlanId,
    currentInstant: Instant,
): DbTrainingPlanWithExercises =
    DbTrainingPlanWithExercises(
        trainingPlan = this.toDbTrainingPlan(id, currentInstant),
        exercises =
            this.exercises.mapIndexed { index, trainingExercise ->
                trainingExercise.toDbTrainingExerciseWithCategories(
                    trainingPlanId = id,
                    position = index,
                )
            },
    )

private fun TrainingPlanConfiguration.toDbTrainingPlan(
    id: TrainingPlanId,
    currentInstant: Instant,
) = DbTrainingPlan(
    id = id.toString(),
    name = title,
    description = description,
    restTime = restTime.inWholeSeconds,
    lastModified = currentInstant,
    lastUsed = null,
)

internal fun DbTrainingPlanWithExercises.toTrainingPlan() =
    TrainingPlan(
        id = TrainingPlanId(trainingPlan.id),
        title = trainingPlan.name,
        description = trainingPlan.description,
        exercises =
            exercises.sortedBy { it.trainingExercise.position }.map {
                it.toTrainingExercise()
            },
        restTime = trainingPlan.restTime.seconds,
        lastModification = trainingPlan.lastModified,
        lastSession = trainingPlan.lastUsed,
    )

internal fun DbTrainingOverview.toTrainingPlanOverview() =
    TrainingPlanOverview(
        id = TrainingPlanId(header.trainingPlanId),
        title = header.title,
        description = header.description,
        categories = categories.map { it.toExerciseCategoryName() }.toSet(),
        lastModification = header.lastModification,
        lastSession = header.lastSession,
    )
