plugins {
    id(BuildPlugins.commonLibraryPlugin)
}

android {
    namespace = "com.lukasz.witkowski.training.planner.statistics"
}

dependencies {
    implementation(projects.training)
    api(projects.shared)

    implementation(libs.bundles.room)
    kapt(libs.androidx.roomCompiler)
    implementation(libs.timber)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinTestJunit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}
