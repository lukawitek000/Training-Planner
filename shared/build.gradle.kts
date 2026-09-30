plugins {
    id(BuildPlugins.commonLibraryPlugin)
}

android {
    namespace = "com.lukasz.witkowski.training.planner.shared"
}

dependencies {
    implementation(libs.timber)
    implementation(libs.androidx.annotation)
    implementation(libs.kotlinx.coroutines.core)
    // koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)

    testImplementation(libs.junit)
}
