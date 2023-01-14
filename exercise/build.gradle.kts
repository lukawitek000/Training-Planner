plugins {
    id(BuildPlugins.commonLibraryPlugin)
}

dependencies {
    api(projects.image)

    // Kotlin reflection - Used to get subclasses of Category sealed class
    implementation(libs.kotlinReflect)

    implementation(libs.timber)
    implementation(libs.bundles.room)
    kapt(libs.androidx.roomCompiler)
}
