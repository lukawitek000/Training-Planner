package com.lukasz.witkowski.training.planner.session.service

import timber.log.Timber

class SessionServiceContainer private constructor(
    val notificationPendingIntentProvider: NotificationPendingIntentProvider,
    val notificationFactory: NotificationFactory
) {

    companion object {
        @Volatile
        private var instance: SessionServiceContainer? = null

        fun getInstance(): SessionServiceContainer {
            return synchronized(this) {
                if (instance == null) {
                    throw IllegalStateException("SessionServiceContainer not initialized.")
                }
                instance!!
            }
        }

        fun initialize(
            notificationPendingIntentProvider: NotificationPendingIntentProvider,
            notificationFactory: NotificationFactory
        ) {
            return synchronized(this) {
                if (instance == null) {
                    instance = SessionServiceContainer(
                        notificationPendingIntentProvider,
                        notificationFactory
                    )
                }
                Timber.w("SessionServiceContainer is already initialized.")
            }
        }
    }
}
