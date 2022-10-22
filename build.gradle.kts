buildscript {
    extra.apply {
        set("kotlin_version", "1.6.10")
        set("compose_version", "1.1.1")
    }
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.38.1")
    }
}

tasks.register("clean",Delete::class){
    delete(rootProject.buildDir)
}
