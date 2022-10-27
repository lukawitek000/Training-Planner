plugins {
    id(BuildPlugins.commonLibraryPlugin)
    id("dagger.hilt.android.plugin")
//    alias(libs.plugins.daggerAndroidPlugin)
}

dependencies {
    implementation(project(":exercise"))
    implementation(project(":synchronization"))
    api(project(":shared"))

    implementation(libs.kotlinx.coroutinesPlayServices)
    implementation(libs.playServicesWearable)
    implementation(libs.timber)
    implementation(libs.bundles.room)
    kapt(libs.androidx.roomCompiler)

    // Hilt - dependency injection
    implementation(libs.dagger.hiltAndroid)
    kapt(libs.dagger.hiltAndroidCompiler)
}
