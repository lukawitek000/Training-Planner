package com.lukasz.witkowski.training.planner.training.domain

import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import java.io.OutputStream

interface TrainingPlanReceiver {
    suspend fun receiveTrainingPlan(inputStream: InputStream, outputStream: OutputStream): Flow<TrainingPlan>
    suspend fun confirmReceivingTrainingPlan(id: TrainingPlanId)
}