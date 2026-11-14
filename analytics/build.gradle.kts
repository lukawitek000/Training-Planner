plugins {
    id(BuildPlugins.commonLibraryPlugin)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.lukasz.witkowski.training.planner.analytics"

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(projects.session)
    implementation(projects.training)
    api(projects.shared)

    // Compose & UI
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.androidx.roomRuntime)
    implementation(libs.androidx.roomPaging)
    implementation(libs.androidx.paging.common)
    implementation(libs.androidx.paging.compose)
    ksp(libs.androidx.roomCompiler)
    implementation(libs.timber)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinTestJunit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.roboelectric)
    testImplementation(libs.androidx.paging.testing)
}
