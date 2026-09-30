plugins {
    id(BuildPlugins.commonLibraryPlugin)
}

android {
    namespace = "com.lukasz.witkowski.training.planner.image"

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(projects.shared)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.timber)
    implementation(libs.androidx.roomRuntime)
    ksp(libs.androidx.roomCompiler)

    // koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    testImplementation(libs.junit)
    testImplementation(libs.roboelectric)
    testImplementation(libs.androidx.testCore)
    testImplementation(libs.kotlinTestJunit)
    // Without live data test is failing https://issuetracker.google.com/issues/237574812
    testImplementation(libs.androidx.lifecycle.livedataKtx)
}
