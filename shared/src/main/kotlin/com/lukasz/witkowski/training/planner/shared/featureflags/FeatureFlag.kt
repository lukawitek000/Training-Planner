package com.lukasz.witkowski.training.planner.shared.featureflags

enum class FeatureFlag(
    val key: String,
    val title: String,
    val description: String,
    val defaultValue: Boolean
) {
    ONLINE_SYNC(
        key = "feature_online_sync",
        title = "Online Cloud Sync & Auth",
        description = "Enables cloud auth, remote exercise sync, and conflict resolution.",
        defaultValue = false
    )
}
