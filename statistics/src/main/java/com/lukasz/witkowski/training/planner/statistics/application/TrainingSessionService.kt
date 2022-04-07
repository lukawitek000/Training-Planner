package com.lukasz.witkowski.training.planner.statistics.application

import com.lukasz.witkowski.training.planner.training.domain.TrainingExercise
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan

class TrainingSessionService {

    private val trainingPlan: TrainingPlan? = null

    fun getNextExercise(): TrainingExercise {
        require(trainingPlan != null)

        return trainingPlan.exercises[0]
    }

    fun getRestTime(): Long {
        require(trainingPlan != null)

        return 0L
    }

    fun startTraining(trainingPlan: TrainingPlan) {

    }
}