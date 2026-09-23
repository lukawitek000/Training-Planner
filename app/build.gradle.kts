plugins {
    id("com.android.application")
    alias(libs.plugins.kotlin.compose)
}

android {
    compileSdk = ConfigData.compileSdk
    namespace = ConfigData.applicationNamespace

    defaultConfig {
        applicationId = ConfigData.applicationId
        minSdk = ConfigData.minSdk
        targetSdk = ConfigData.targetSdk
        versionCode = ConfigData.versionCode
        versionName = ConfigData.versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(projects.exercise)
    implementation(projects.training)
    implementation(projects.statistics)

    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodelKtx)
    implementation(libs.androidx.activityKtx)
    implementation(libs.timber)
    implementation(libs.coil)
    implementation(libs.bytebeats.charts)

//    implementation(libs.google.material)
//    implementation(libs.androidx.compose.ui)
//    implementation(libs.androidx.compose.material)
//    debugImplementation(libs.androidx.compose.ui.tooling)
//    implementation(libs.androidx.compose.ui.tooling.preview)
//    implementation(libs.androidx.compose.material.icons.extended)
//    implementation(libs.androidx.compose.navigation)
//    implementation(libs.androidx.activity.compose)
//    implementation(libs.androidx.compose.lifecycleViewmodelCompose)
    implementation(platform(libs.compose.bom))

    implementation(libs.compose.ui)
    implementation(libs.compose.material)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material.icons.extended)

    implementation(libs.androidx.compose.navigation)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.lifecycleViewmodelCompose)

    implementation(libs.google.material)

    // Without this dependency there is a build error
    implementation(libs.google.playServicesWearable)
}
