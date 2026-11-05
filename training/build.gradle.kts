plugins {
    id(BuildPlugins.commonLibraryPlugin)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.lukasz.witkowski.training.planner.training"

    buildFeatures {
        compose = true
    }
}

dependencies {
    api(projects.shared)
    api(projects.exercise)

    // Compose & UI
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    // Lifecycle & Koin Compose
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodelKtx)
    implementation(libs.androidx.compose.lifecycleViewmodelCompose)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)

    // Reorderable list for editor
    implementation(libs.calvin.reorderable)

    implementation(libs.timber)
    implementation(libs.androidx.roomRuntime)
    ksp(libs.androidx.roomCompiler)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    testImplementation(libs.kotlinTestJunit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.roboelectric)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
}
