plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":training"))
    implementation(project(":exercise"))
    implementation(libs.playServicesWearable)
    implementation(libs.androidx.wear)
    implementation(libs.material)
    implementation(libs.androidx.activityKtx)
    implementation(libs.timber)

    // Hilt - dependency injection
    implementation(libs.dagger.hiltAndroid)
    kapt(libs.dagger.hiltAndroidCompiler)
}