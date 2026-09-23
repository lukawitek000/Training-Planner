buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.gradlePlugin.android)
        classpath(libs.gradlePlugin.kotlin)
        classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.3.6")
    }
}

plugins {
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.jetbrains.kotlin.serialization) apply false
}

tasks.register("clean",Delete::class){
    delete(rootProject.layout.buildDirectory)
}
