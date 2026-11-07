package com.lukasz.witkowski.training.planner.session.domain.session

import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExercise
import com.lukasz.witkowski.training.planner.session.domain.session.models.SessionExerciseConfiguration
import com.lukasz.witkowski.training.planner.session.domain.session.models.TrainingExerciseSnapshot
import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan
import kotlin.time.Duration

/**
 * Exercises are performed by completing all sets of one exercise before moving to the next.
 * This is the standard method for strength training and aligns with the active workout UI.
 */
class StraightSetsPolicy : TrainingSetsPolicy {
    override fun loadExercises(trainingPlan: TrainingPlan): List<SessionExercise> {
        val allExercises = mutableListOf<SessionExercise>()
        for (exercise in trainingPlan.exercises) {
            repeat(exercise.sets) { idx ->
                val isLast = idx == exercise.sets - 1
                val restTime = if (isLast) {
                    trainingPlan.restTime
                } else {
                    exercise.restTime
                }
                allExercises.add(
                    exercise.toSessionExercise(restTime)
                )
            }
        }
        return allExercises
    }

    private fun TrainingExercise.toSessionExercise(restTime: Duration): SessionExercise =
        SessionExercise(
            snapshot = toSnapshot(),
            currentConfig = toConfig(),
            restTime = restTime
        )

    private fun TrainingExercise.toSnapshot(): TrainingExerciseSnapshot = TrainingExerciseSnapshot(
        id = id,
        name = exercise.name,
        description = exercise.description,
        categories = exercise.categories,
        imageIds = exercise.imageIds,
        plannedConfig = toConfig()
    )

    private fun TrainingExercise.toConfig(): SessionExerciseConfiguration =
        SessionExerciseConfiguration(
            reps = repetitions,
            weightInKg = weightInKg?.toDouble(),
            holdTime = time.takeIf { it > Duration.ZERO }
        )
}
