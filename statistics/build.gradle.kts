plugins {
    id(BuildPlugins.commonLibraryPlugin)
    id("dagger.hilt.android.plugin")
//    alias(libs.plugins.daggerAndroidPlugin)
}

dependencies {
    implementation(project(":training"))
    implementation(project(":exercise"))
    implementation(project(":shared"))

    testImplementation(libs.junit)
    testImplementation(libs.kotlinTestJunit)

    // Hilt - dependency injection
    implementation(libs.dagger.hiltAndroid)
    kapt(libs.dagger.hiltAndroidCompiler)

    implementation(libs.bundles.room)
    kapt(libs.androidx.roomCompiler)
}
