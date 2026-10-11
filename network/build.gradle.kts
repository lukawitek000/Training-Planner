plugins {
    id(BuildPlugins.commonLibraryPlugin)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    namespace = "com.lukasz.witkowski.training.planner.network"
}

dependencies {
    api(projects.shared)
    api(projects.dto)

    // Retrofit & OkHttp
    api(libs.retrofit)
    api(libs.retrofit.converter.kotlinxSerialization)
    api(libs.okhttp)
    api(libs.okhttp.loggingInterceptor)
    api(libs.kotlinx.serialization.json)

    // Coroutines & Koin
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.timber)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)

    // Testing
    testImplementation(libs.kotlinTestJunit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.okhttp.mockwebserver)
}
