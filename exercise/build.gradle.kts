plugins {
    id(BuildPlugins.commonLibraryPlugin)
    id(BuildPlugins.detektPlugin)
}

android {
    namespace = "com.lukasz.witkowski.training.planner.exercise"
}

dependencies {
    api(projects.image)

    // Kotlin reflection - Used to get subclasses of Category sealed class
    implementation(libs.kotlinReflect)

    detektPlugins(libs.detektFormatting)
    implementation(libs.timber)
    implementation(libs.bundles.room)
    kapt(libs.androidx.roomCompiler)
}
