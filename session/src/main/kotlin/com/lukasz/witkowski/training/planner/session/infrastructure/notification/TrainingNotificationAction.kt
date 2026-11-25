package com.lukasz.witkowski.training.planner.session.infrastructure.notification

enum class TrainingNotificationAction(val intentAction: String, val requestCode: Int) {
    SKIP(
        intentAction = "com.lukasz.witkowski.training.planner.session.action.SKIP",
        requestCode = 2
    ),
    COMPLETE(
        intentAction = "com.lukasz.witkowski.training.planner.session.action.COMPLETE",
        requestCode = 3
    ),
    START_TIMER(
        intentAction = "com.lukasz.witkowski.training.planner.session.action.START_TIMER",
        requestCode = 4
    ),
    RESET_TIMER(
        intentAction = "com.lukasz.witkowski.training.planner.session.action.RESET_TIMER",
        requestCode = 5
    );

    companion object {
        fun fromIntentAction(action: String?): TrainingNotificationAction? =
            entries.firstOrNull { it.intentAction == action }
    }
}
