package com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi

sealed class SynchronizationStatus {
    data class SuccessfulSynchronization<T>(val id: T): SynchronizationStatus()

    data class FailureSynchronization(val exception: SynchronizationException): SynchronizationStatus()
}




