plugins {
    id(BuildPlugins.commonLibraryPlugin)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    namespace = "com.lukasz.witkowski.training.planner.exercise"
}

dependencies {
    api(projects.image)
    api(projects.dto)
    api(projects.network)
    api(projects.user)
    implementation(projects.shared)

    // Retrofit & Serialization
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinxSerialization)
    implementation(libs.kotlinx.serialization.json)

    // Koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    // Kotlin reflection - Used to get subclasses of Category sealed class
    implementation(libs.kotlinReflect)
    implementation(libs.androidx.paging.common)
    implementation(libs.androidx.roomPaging)
    implementation(libs.androidx.workRuntime)

    implementation(libs.timber)
    implementation(libs.androidx.roomRuntime)
    ksp(libs.androidx.roomCompiler)

    testImplementation(libs.kotlinTestJunit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.roboelectric)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.paging.testing)
    testImplementation(libs.okhttp.mockwebserver)
}
