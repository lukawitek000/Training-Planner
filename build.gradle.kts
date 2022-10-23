val composeVersion by extra("1.1.1")
val kotlinVersion by extra("1.6.10")

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.android.gradlePlugin)
        classpath(libs.kotlin.gradlePlugin)
        classpath(libs.hilt.gradlePlugin)
    }
}

tasks.register("clean",Delete::class){
    delete(rootProject.buildDir)
}
