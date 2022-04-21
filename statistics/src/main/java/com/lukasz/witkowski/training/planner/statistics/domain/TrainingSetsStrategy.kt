package com.lukasz.witkowski.training.planner.statistics.domain

import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingExerciseId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan

/**
 * Defines what is the order of the exercises in the training.
 * Look at https://en.fitnessyard.com/the-knowledge/how-to-build-your-muscles/types-of-workout-sets
 */
interface TrainingSetsStrategy {
    fun loadExercises(trainingPlan: TrainingPlan): List<TrainingExercise>
}
