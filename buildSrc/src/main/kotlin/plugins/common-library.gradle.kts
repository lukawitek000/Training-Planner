package plugins

import ConfigData
import gradle.kotlin.dsl.accessors._c7cd46b2e483aef787386d5c14b0a82d.android
import gradle.kotlin.dsl.accessors._c7cd46b2e483aef787386d5c14b0a82d.kotlinOptions
import org.gradle.kotlin.dsl.kotlin

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {
    compileSdk = ConfigData.compileSdk

    defaultConfig {
        minSdk = ConfigData.minSdk
        targetSdk = ConfigData.targetSdk

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
}
