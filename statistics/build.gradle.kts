plugins {
    id(BuildPlugins.commonLibraryPlugin)
}

dependencies {
    implementation(projects.training)
    api(projects.shared)

    implementation(libs.bundles.room)
    kapt(libs.androidx.roomCompiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinTestJunit)
    testImplementation(libs.kotlinx.coroutines.test)
}
