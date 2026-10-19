package com.lukasz.witkowski.training.planner.network

interface SyncScheduler {
    fun enqueueMutationSync()

    fun enqueueRemoteChangeSync(force: Boolean = false)

    fun cancelMutationSync()
}
