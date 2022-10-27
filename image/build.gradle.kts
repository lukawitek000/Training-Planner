
plugins {
    id(BuildPlugins.commonLibraryPlugin)
    id(BuildPlugins.detektPlugin)
    id(libs.plugins.detekt.get().pluginId)
}

val kotlinVersion: String by rootProject.extra

android {
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
