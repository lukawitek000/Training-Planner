plugins {
    id(BuildPlugins.commonLibraryPlugin)
}

android {
    namespace = "com.lukasz.witkowski.training.planner.shared"
}

dependencies {
    implementation(libs.timber)

    testImplementation(libs.junit)
}
