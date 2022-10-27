val composeVersion by extra("1.1.1")
val kotlinVersion by extra("1.6.10")

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.gradlePlugin.android)
        classpath(libs.gradlePlugin.kotlin)
        classpath(libs.gradlePlugin.hilt)
    }
}

tasks.register("clean",Delete::class){
    delete(rootProject.buildDir)
}
