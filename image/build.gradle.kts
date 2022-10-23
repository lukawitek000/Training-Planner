import io.gitlab.arturbosch.detekt.Detekt

plugins {
    id(BuildPlugins.commonLibraryPlugin)
    id("io.gitlab.arturbosch.detekt") version "1.21.0"
}

val kotlinVersion: String by rootProject.extra

android {
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("com.jakewharton.timber:timber:5.0.1")

    val roomVersion = "2.4.3"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.21.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.9")
    testImplementation("androidx.test:core:1.4.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    // Without live data test is failing https://issuetracker.google.com/issues/237574812
    testImplementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
}

tasks.register<Detekt>("customDetekt") {
    val configFile = files("$rootDir/config/detekt/detekt.yml")
    description = "Custom DETEKT build for all modules"
    parallel = true
    buildUponDefaultConfig = false
    setSource(files("src/main/kotlin", "src/main/java"))
    config.setFrom(configFile)
    reports {
        html.enabled = true
        xml.enabled = false
        txt.enabled = false
    }
}
