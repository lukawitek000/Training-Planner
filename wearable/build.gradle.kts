plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = ConfigData.compileSdk

    defaultConfig {
        applicationId = ConfigData.applicationId
        minSdk = ConfigData.wearableMinSdk
        targetSdk = ConfigData.targetSdk
        versionCode = ConfigData.versionCode
        versionName = ConfigData.versionName
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += kotlin.collections.listOf<String>(
            "-Xjvm-default=enable",
            "-Xextended-compiler-checks"
        )

    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(projects.training)
    implementation(projects.statistics)

    implementation(libs.google.playServicesWearable)
    implementation(libs.androidx.wear)
    implementation(libs.google.material)
    implementation(libs.androidx.activityKtx)
    implementation(libs.androidx.fragmentKtx)
    implementation(libs.androidx.lifecycle.livedataKtx)
    implementation(libs.timber)
}
