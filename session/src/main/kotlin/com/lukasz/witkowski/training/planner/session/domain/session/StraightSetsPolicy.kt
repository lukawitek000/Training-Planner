package com.lukasz.witkowski.training.planner.session.domain.session

import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan

/**
 * Exercises are performed by completing all sets of one exercise before moving to the next.
 * This is the standard method for strength training and aligns with the active workout UI.
 */
class StraightSetsPolicy : TrainingSetsPolicy {
    override fun loadExercises(trainingPlan: TrainingPlan): List<TrainingExercise> {
        val allExercises = mutableListOf<TrainingExercise>()
        for (exercise in trainingPlan.exercises) {
            repeat(exercise.sets) {
                allExercises.add(exercise)
            }
        }
        return allExercises
    }
}
