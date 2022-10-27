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
    implementation(project(":exercise"))
    implementation(project(":training"))
    implementation(project(":statistics"))

    // Hilt - dependency injection
    implementation(libs.dagger.hiltAndroid)
    kapt(libs.dagger.hiltAndroidCompiler)

    implementation("com.google.android.material:material:1.7.0")
//
    implementation("androidx.compose.ui:ui:1.1.1")
    implementation("androidx.compose.material:material:1.1.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.1.1")
    debugImplementation("androidx.compose.ui:ui-tooling:1.1.1")

    implementation("androidx.activity:activity-compose:1.6.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
//
    implementation("androidx.navigation:navigation-compose:2.5.2")
    implementation("androidx.compose.material:material-icons-extended:1.2.1")

    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation(libs.timber)
//
    implementation("androidx.activity:activity-ktx:1.6.0")
    implementation("com.github.skydoves:landscapist-coil:2.0.0")
//
//    // Wearables
    // Without this dependency there is a build error
    implementation("com.google.android.gms:play-services-wearable:18.0.0")

//    // Plots
    implementation("io.github.bytebeats:compose-charts:0.1.0")
}