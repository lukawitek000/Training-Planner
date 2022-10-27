plugins {
    id(BuildPlugins.commonLibraryPlugin)
    id("dagger.hilt.android.plugin")
//    alias(libs.plugins.daggerAndroidPlugin)
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

    // Hilt - dependency injection
    implementation(libs.google.dagger.hiltAndroid)
    kapt(libs.google.dagger.hiltAndroidCompiler)
}
