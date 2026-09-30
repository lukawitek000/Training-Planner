plugins {
    id(BuildPlugins.commonLibraryPlugin)
}

android {
    namespace = "com.lukasz.witkowski.training.planner.exercise"
}

dependencies {
    api(projects.image)

    // Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)

    // Kotlin reflection - Used to get subclasses of Category sealed class
    implementation(libs.kotlinReflect)
    implementation(libs.androidx.paging.common)
    implementation(libs.androidx.roomPaging)

    implementation(libs.timber)
    implementation(libs.androidx.roomRuntime)
    ksp(libs.androidx.roomCompiler)
}
