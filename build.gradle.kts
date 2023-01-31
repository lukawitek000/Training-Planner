buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.gradlePlugin.android)
        classpath(libs.gradlePlugin.kotlin)
    }
}

tasks.register("clean",Delete::class){
    delete(rootProject.buildDir)
}
