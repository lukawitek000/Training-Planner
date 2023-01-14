plugins {
    id(BuildPlugins.commonLibraryPlugin)
}

dependencies {
    implementation(projects.training)
    api(projects.shared)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinTestJunit)

    implementation(libs.bundles.room)
    kapt(libs.androidx.roomCompiler)
}
