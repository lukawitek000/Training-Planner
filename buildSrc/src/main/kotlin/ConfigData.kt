object ConfigData {
    const val compileSdk = 37
    const val minSdk = 28
    const val wearableMinSdk = 30
    const val targetSdk = 36

    const val applicationNamespace = "com.lukasz.witkowski.training.planner"
    const val applicationId = "com.lukasz.witkowski.training.planner"

    const val VERSION_MAJOR = 0
    const val VERSION_MINOR = 1
    const val VERSION_PATCH = 0
    const val VERSION_BUILD = 1
    const val VERSION_STAGE = "alpha01"

    val versionCode: Int
        get() = VERSION_MAJOR * 1_000_000 + VERSION_MINOR * 10_000 + VERSION_PATCH * 100 + VERSION_BUILD

    val versionName: String
        get() = if (VERSION_STAGE.isEmpty()) {
            "$VERSION_MAJOR.$VERSION_MINOR.$VERSION_PATCH"
        } else {
            "$VERSION_MAJOR.$VERSION_MINOR.$VERSION_PATCH-$VERSION_STAGE"
        }
}
