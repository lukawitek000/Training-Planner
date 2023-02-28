plugins {
    id(BuildPlugins.commonLibraryPlugin)
    id(BuildPlugins.detektPlugin)
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
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.timber)
    implementation(libs.bundles.room)
    kapt(libs.androidx.roomCompiler)
    detektPlugins(libs.detektFormatting)

    testImplementation(libs.junit)
    testImplementation(libs.roboelectric)
    testImplementation(libs.androidx.testCore)
    testImplementation(libs.kotlinTestJunit)
    // Without live data test is failing https://issuetracker.google.com/issues/237574812
    testImplementation(libs.androidx.lifecycle.livedataKtx)
}
