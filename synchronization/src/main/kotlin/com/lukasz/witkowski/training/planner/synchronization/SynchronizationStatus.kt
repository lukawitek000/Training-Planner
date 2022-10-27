package com.lukasz.witkowski.training.planner.synchronization

sealed class SynchronizationStatus<T>(val id: T) {
    data class Successful<T>(val value: T) : SynchronizationStatus<T>(value)
    data class Failure<T>(val value: T, val exception: SynchronizationException) :
        SynchronizationStatus<T>(value)
}
