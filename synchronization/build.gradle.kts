plugins {
    id(BuildPlugins.commonLibraryPlugin)
}

dependencies {
    implementation(libs.coroutinesCore)
    implementation(libs.kotlinx.coroutinesPlayServices)
    implementation(libs.playServicesWearable)
    implementation(libs.gson)
    implementation(libs.timber)
}
