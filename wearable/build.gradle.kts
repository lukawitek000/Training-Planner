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
        freeCompilerArgs += "-Xjvm-default=enable"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(projects.training)

    implementation(libs.google.playServicesWearable)
    implementation(libs.androidx.wear)
    implementation(libs.google.material)
    implementation(libs.androidx.activityKtx)
    implementation(libs.timber)
}
