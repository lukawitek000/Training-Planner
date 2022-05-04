package com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi

import java.lang.RuntimeException

sealed class SynchronizationResponse {
    data class SuccessfulSynchronization<T>(val id: T): SynchronizationResponse()

    data class FailureSynchronization(val exception: SynchronizationException): SynchronizationResponse()
}




