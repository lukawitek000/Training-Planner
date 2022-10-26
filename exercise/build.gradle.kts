plugins {
    id(BuildPlugins.commonLibraryPlugin)
}

dependencies {
    implementation(project(":shared"))
    api(project(":image"))

    // Kotlin reflection - Used to get subclasses of Category sealed class
    implementation(libs.kotlinReflect)

    implementation(libs.timber)
    implementation(libs.bundles.room)
    kapt(libs.androidx.roomCompiler)

    // Hilt - dependency injection
    implementation(libs.dagger.hiltAndroid)
    kapt(libs.dagger.hiltAndroidCompiler)
}
