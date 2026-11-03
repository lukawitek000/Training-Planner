plugins {
    id(BuildPlugins.commonLibraryPlugin)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.lukasz.witkowski.training.planner.session"

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(projects.training)
    implementation(projects.exercise)
    api(projects.shared)

    implementation(libs.timber)

    implementation(libs.coil.compose)
    implementation(libs.bytebeats.charts)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinTestJunit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.roboelectric)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
