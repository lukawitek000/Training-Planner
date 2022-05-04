package com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi

interface ResponseHandler<T> {

    fun handleSynchronizationResponse(data: T)
}