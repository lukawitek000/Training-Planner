plugins {
    id("com.android.application")
    id("io.insert-koin.compiler.plugin")
}

android {
    compileSdk = ConfigData.compileSdk
    namespace = ConfigData.applicationNamespace

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
        buildConfig = true
    }
}

dependencies {
    implementation(projects.training)
    implementation(projects.statistics)
    implementation(projects.sessionService)

    implementation(libs.google.playServicesWearable)
    implementation(libs.androidx.wear)
    implementation(libs.androidx.wearOngoing)
    implementation(libs.google.material)
    implementation(libs.androidx.activityKtx)
    implementation(libs.androidx.fragmentKtx)
    implementation(libs.androidx.lifecycle.livedataKtx)
    implementation(libs.timber)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
}
