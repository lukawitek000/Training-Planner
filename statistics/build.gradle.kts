plugins {
    id(BuildPlugins.commonLibraryPlugin)
    id(BuildPlugins.detektPlugin)
}

android {
    namespace = "com.lukasz.witkowski.training.planner.statistics"
}

dependencies {
    implementation(projects.training)
    api(projects.shared)

    implementation(libs.bundles.room)
    kapt(libs.androidx.roomCompiler)

//    detektPlugins(libs.detektFormatting)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinTestJunit)
    testImplementation(libs.kotlinx.coroutines.test)
}
