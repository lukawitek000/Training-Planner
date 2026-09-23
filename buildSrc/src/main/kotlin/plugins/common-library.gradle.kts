package plugins

import ConfigData
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.kotlin.dsl.configure

plugins {
    id("com.android.library")
    //id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

extensions.configure<LibraryExtension> {
    compileSdk = ConfigData.compileSdk

    defaultConfig {
        minSdk = ConfigData.minSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// Access the version catalog using VersionCatalogsExtension
val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    // Look up libraries from gradle/libs.versions.toml dynamically
    libs.findLibrary("kotlin-stdlib").ifPresent { stdlib ->
        add("implementation", stdlib)
    }

    // Optional: add Kotlin BOM if defined in catalog
    libs.findLibrary("kotlin-bom").ifPresent { bom ->
        add("implementation", platform(bom))
    }
}
