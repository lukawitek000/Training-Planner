package com.lukasz.witkowski.training.planner.session.domain.session

import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan

/**
 * Exercises are performed in pairs (supersets) alternating between exercises in the pair,
 * then proceeding to the next pair.
 */
class SupersetSetsPolicy : TrainingSetsPolicy {
    override fun loadExercises(trainingPlan: TrainingPlan): List<TrainingExercise> {
        val allExercises = mutableListOf<TrainingExercise>()
        val pairs = trainingPlan.exercises.chunked(2)
        for (pair in pairs) {
            val maxSets = pair.maxOfOrNull { it.sets } ?: 0
            for (set in 1..maxSets) {
                for (exercise in pair) {
                    if (exercise.sets >= set) {
                        allExercises.add(exercise)
                    }
                }
            }
        }
        return allExercises
    }
}
