plugins {
    id(BuildPlugins.commonLibraryPlugin)
}

dependencies {
    implementation(projects.synchronization)
    api(projects.shared)
    api(projects.exercise)

    implementation(libs.kotlinx.coroutines.playServices)
    implementation(libs.google.playServicesWearable)
    implementation(libs.timber)
    implementation(libs.bundles.room)
    kapt(libs.androidx.roomCompiler)
}
