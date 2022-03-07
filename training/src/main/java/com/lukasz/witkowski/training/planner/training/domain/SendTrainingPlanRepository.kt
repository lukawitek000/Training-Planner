package com.lukasz.witkowski.training.planner.training.domain

import kotlinx.coroutines.flow.Flow

// TODO how to name this repository??TrainingPlanSendER  Connector, calculatorm transformer
// Repo = read update delete - utrwalanie stanu, przechowywanie stanu / danych
interface SendTrainingPlanRepository {
    fun send(trainingPlans: List<TrainingPlan>): Flow<String>
}