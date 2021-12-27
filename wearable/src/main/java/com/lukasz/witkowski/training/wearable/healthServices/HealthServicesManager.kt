package com.lukasz.witkowski.training.wearable.healthServices

import android.util.Log
import androidx.concurrent.futures.await
import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseInfo
import androidx.health.services.client.data.ExerciseTrackedStatus
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseTypeCapabilities
import com.lukasz.witkowski.shared.models.Category
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber
import javax.inject.Inject

class HealthServicesManager @Inject constructor(
    healthServicesClient: HealthServicesClient,
    coroutineScope: CoroutineScope
) {

    private val exerciseClient = healthServicesClient.exerciseClient

    private var exerciseCapabilities: ExerciseTypeCapabilities? = null
    private var areCapabilitiesLoaded = false

    suspend fun getExerciseCapabilities(exerciseType: ExerciseType): ExerciseTypeCapabilities? {
        if(!areCapabilitiesLoaded) {
            val capabilities = exerciseClient.capabilities.await()
            if (exerciseType in capabilities.supportedExerciseTypes) {
                exerciseCapabilities = capabilities.getExerciseTypeCapabilities(exerciseType)
            }
            areCapabilitiesLoaded = true
        }
        return exerciseCapabilities
    }

    suspend fun hasExerciseCapability(exercise: Category): Boolean {
        return getExerciseCapabilities(getExerciseType(exercise)) != null
    }

    suspend fun isExerciseInProgress(): Boolean {
        val exerciseInfo = getExerciseInfo()
        return exerciseInfo.exerciseTrackedStatus == ExerciseTrackedStatus.OWNED_EXERCISE_IN_PROGRESS
    }

    suspend fun isTrackingExerciseInAnotherApp(): Boolean {
        val exerciseInfo = getExerciseInfo()
        return exerciseInfo.exerciseTrackedStatus == ExerciseTrackedStatus.OTHER_APP_IN_PROGRESS
    }

    suspend fun startExercise(exercise: Category) {
        Timber.d("Starting exercise")
        // Types for which we want to receive metrics. Only ask for ones that are supported.
        val exerciseType = getExerciseType(exercise)
        val capabilities = getExerciseCapabilities(exerciseType) ?: return
        val dataTypes = setOf(
            DataType.HEART_RATE_BPM,
        ).intersect(capabilities.supportedDataTypes)

        val aggDataTypes = setOf(
            DataType.TOTAL_CALORIES,
            DataType.HEART_RATE_BPM
        ).intersect(capabilities.supportedDataTypes)

        val config = ExerciseConfig.builder()
            .setExerciseType(exerciseType)
            .setShouldEnableAutoPauseAndResume(false)
            .setAggregateDataTypes(aggDataTypes)
            .setDataTypes(dataTypes)
            .build()
        exerciseClient.startExercise(config).await()
    }

    suspend fun endExercise() {
        Timber.d("End exercise")
        exerciseClient.endExercise().await()
    }



    private suspend fun getExerciseInfo(): ExerciseInfo {
        return exerciseClient.currentExerciseInfo.await()
    }

    private fun getExerciseType(exercise: Category): ExerciseType {
        // TODO select exercise type based on category
        return ExerciseType.CALISTHENICS
    }

}