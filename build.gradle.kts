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
