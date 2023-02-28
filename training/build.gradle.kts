plugins {
    id(BuildPlugins.commonLibraryPlugin)
    id(BuildPlugins.detektPlugin)
}

android {
    namespace = "com.lukasz.witkowski.training.planner.training"
}

dependencies {
    implementation(projects.synchronization)
    api(projects.shared)
    api(projects.exercise)

    detektPlugins(libs.detektFormatting)
    implementation(libs.kotlinx.coroutines.playServices)
    implementation(libs.google.playServicesWearable)
    implementation(libs.timber)
    implementation(libs.bundles.room)
    kapt(libs.androidx.roomCompiler)
}
