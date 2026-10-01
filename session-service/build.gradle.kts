plugins {
    id(BuildPlugins.commonLibraryPlugin)
}

android {
    namespace = "com.lukasz.witkowski.training.planner.session.service"
}

dependencies {
    implementation(projects.statistics)
    implementation(projects.training)

    implementation(libs.androidx.core)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.timber)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
}
