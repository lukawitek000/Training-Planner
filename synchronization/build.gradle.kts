plugins {
    id(BuildPlugins.commonLibraryPlugin)
}

android {
    namespace = "com.lukasz.witkowski.training.planner.synchronization"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.playServices)
    implementation(libs.google.playServicesWearable)
    implementation(libs.google.gson)
    implementation(libs.timber)
}
