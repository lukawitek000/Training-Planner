plugins {
    id(BuildPlugins.commonLibraryPlugin)
    id("dagger.hilt.android.plugin")
//    alias(libs.plugins.daggerAndroidPlugin)
}

dependencies {
    implementation(projects.training)
    api(projects.shared)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinTestJunit)

    // Hilt - dependency injection
    implementation(libs.google.dagger.hiltAndroid)
    kapt(libs.google.dagger.hiltAndroidCompiler)

    implementation(libs.bundles.room)
    kapt(libs.androidx.roomCompiler)
}
