package plugins

import BuildPlugins
import ConfigData
import com.android.build.gradle.LibraryExtension
import org.gradle.accessors.dm.LibrariesForLibs
import gradle.kotlin.dsl.accessors._6b1cdd1e881959619ea23cf7941079a9.detekt
import gradle.kotlin.dsl.accessors._6b1cdd1e881959619ea23cf7941079a9.detektPlugins
import org.gradle.kotlin.dsl.kotlin

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("org.jlleitschuh.gradle.ktlint")
//    id("org.jlleitschuh.gradle.ktlint")
}

apply(plugin = BuildPlugins.detektPlugin)

configure<LibraryExtension> {
    compileSdk = ConfigData.compileSdk

    defaultConfig {
        minSdk = ConfigData.minSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    val libs = the<LibrariesForLibs>()
    dependencies {
        detektPlugins(libs.detektFormatting)
    }
}
