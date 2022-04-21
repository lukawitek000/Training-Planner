package com.lukasz.witkowski.training.planner.statistics.domain

import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan

/**
 * All exercises are performed in succession
 */
class CircuitSetsStrategy : TrainingSetsStrategy {

    override fun loadExercises(trainingPlan: TrainingPlan): List<TrainingExercise> {
        val maxSets = trainingPlan.exercises.maxOf { it.sets }
        val allExercises = mutableListOf<TrainingExercise>()
        for (set in 1..maxSets) {
            val setExercises = trainingPlan.exercises.filter { it.sets >= set }
            allExercises.addAll(setExercises)
        }
        return allExercises
    }
}
