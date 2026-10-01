plugins {
    id(BuildPlugins.commonLibraryPlugin)
}

android {
    namespace = "com.lukasz.witkowski.training.planner.statistics"
}

dependencies {
    implementation(projects.training)
    api(projects.shared)

    implementation(libs.androidx.roomRuntime)
    ksp(libs.androidx.roomCompiler)
    implementation(libs.timber)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinTestJunit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}
