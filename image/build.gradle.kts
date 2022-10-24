import io.gitlab.arturbosch.detekt.Detekt

plugins {
    id(BuildPlugins.commonLibraryPlugin)
    alias(libs.plugins.detekt)
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
    implementation(libs.coroutinesCore)
    implementation(libs.timber)
    implementation(libs.bundles.room)
    kapt(libs.androidx.roomCompiler)
    detektPlugins(libs.detektFormatting)

    testImplementation(libs.junit)
    testImplementation(libs.roboelectric)
    testImplementation(libs.androidx.testCore)
    testImplementation(libs.kotlinTestJunit)
    // Without live data test is failing https://issuetracker.google.com/issues/237574812
    testImplementation(libs.androidx.lifecycleLivedataKtx)
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
