dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Training Planner"

include(":app")
include(":wearable")
include(":shared")
include(":exercise")
include(":training")
include(":statistics")
include(":synchronization")
include(":image")
