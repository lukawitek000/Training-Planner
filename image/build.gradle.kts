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
    // TODO use version catalog everywhere
    implementation(libs.coroutines.core)
    implementation(libs.timber)
    implementation(libs.bundles.room)
    kapt(libs.androidx.roomCompiler)
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
