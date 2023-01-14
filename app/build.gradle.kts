plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = ConfigData.compileSdk

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

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += "-Xjvm-default=enable"
    }

    buildFeatures {
        compose = true
    }

    packagingOptions {
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

    implementation(libs.google.material)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material)
    debugImplementation(libs.androidx.compose.uiTooling)
    implementation(libs.androidx.compose.uiToolingPreview)
    implementation(libs.androidx.compose.materialIconsExtended)
    implementation(libs.androidx.compose.navigation)
    // https://programmer.ink/think/a-new-way-to-create-a-viewmodel-creationextras.html
    implementation(libs.androidx.compose.activity) // is it required? works without
    implementation(libs.androidx.compose.lifecycleViewmodelCompose) // is it required? works without

    // Without this dependency there is a build error
    implementation(libs.google.playServicesWearable)
}
