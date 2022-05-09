package com.lukasz.witkowski.training.planner.training.infrastructure.wearableApi

open class SynchronizationException(message: String? = null, throwable: Throwable? = null) :
    Exception(message, throwable)

class SynchronizationSendingException(message: String?, throwable: Throwable? = null) :
    SynchronizationException(message, throwable)

class SynchronizationSavingException(message: String?, throwable: Throwable? = null) :
    SynchronizationException(message, throwable)