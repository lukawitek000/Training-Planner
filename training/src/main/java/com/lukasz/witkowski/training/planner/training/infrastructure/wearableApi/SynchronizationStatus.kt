package com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi

sealed class SynchronizationStatus {
    data class Successful<T>(val id: T) : SynchronizationStatus()
    data class Failure(val exception: SynchronizationException) : SynchronizationStatus()
}
