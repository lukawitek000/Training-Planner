plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.plugins.android.application.get().let { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" })
    implementation(libs.plugins.android.library.get().let { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" })
    implementation(libs.plugins.ksp.get().let { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" })
    // Android Gradle Plugin API
//    implementation("com.android.tools.build:gradle:9.3.2")

    // KSP Gradle Plugin
//    implementation("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.0.20-1.0.25")
}

//dependencies {
//    implementation(libs.gradlePlugin.android)
//    implementation(libs.gradlePlugin.kotlin)
//    implementation(libs.gradlePlugin.detekt)
//    implementation(libs.gradlePlugin.ktlint)
    // Required for the workaround to use version catalog in buildSrc https://github.com/gradle/gradle/issues/15383
//    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    // for ksp
//    implementation("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.0.21-1.0.25")
//}
