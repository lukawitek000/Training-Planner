dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
// Enable Gradle's version catalog support
// https://docs.gradle.org/current/userguide/platforms.html
enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "Training-Planner"

include(":app")
include(":wearable")
include(":shared")
include(":exercise")
include(":training")
include(":statistics")
include(":synchronization")
include(":image")
include(":session-service")
