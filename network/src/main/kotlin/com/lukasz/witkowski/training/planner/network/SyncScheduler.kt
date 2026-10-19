package com.lukasz.witkowski.training.planner.network

interface SyncScheduler {
    fun enqueueMutationSync()

    fun cancelMutationSync()
}
