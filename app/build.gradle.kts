import java.util.Properties

plugins {
    id("com.android.application")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    id("io.insert-koin.compiler.plugin")
}

val resolvedVersionCode = (project.findProperty("versionCodeOverride") as? String)?.toIntOrNull()
    ?: ConfigData.versionCode
val resolvedVersionName = (project.findProperty("versionNameOverride") as? String)
    ?: ConfigData.versionName

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

val releaseStoreFilePath = System.getenv("SIGNING_STORE_FILE")
    ?: keystoreProperties.getProperty("storeFile")
val releaseStorePassword = System.getenv("SIGNING_STORE_PASSWORD")
    ?: keystoreProperties.getProperty("storePassword")
val releaseKeyAlias = System.getenv("SIGNING_KEY_ALIAS")
    ?: keystoreProperties.getProperty("keyAlias")
val releaseKeyPassword = System.getenv("SIGNING_KEY_PASSWORD")
    ?: keystoreProperties.getProperty("keyPassword")

val releaseStoreFile = releaseStoreFilePath?.let { path ->
    val file = File(path)
    if (file.isAbsolute) file else rootProject.file(path)
}

val hasValidReleaseSigning = releaseStoreFile?.exists() == true &&
    !releaseStorePassword.isNullOrBlank() &&
    !releaseKeyAlias.isNullOrBlank() &&
    !releaseKeyPassword.isNullOrBlank()

android {
    compileSdk = ConfigData.compileSdk
    namespace = ConfigData.applicationNamespace

    defaultConfig {
        applicationId = ConfigData.applicationId
        minSdk = ConfigData.minSdk
        targetSdk = ConfigData.targetSdk
        versionCode = resolvedVersionCode
        versionName = resolvedVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        if (hasValidReleaseSigning) {
            create("release") {
                storeFile = releaseStoreFile
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            resValue("string", "app_name", "Training Planner (Debug)")

            // Local Ktor backend server for Emulator
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/\"")
        }
        getByName("release") {
            resValue("string", "app_name", "Training Planner")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            if (hasValidReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            } else {
                logger.info("Release signing credentials not found; release build will remain unsigned or use default fallback.")
            }

            // Production server URL (replace with actual production endpoint in the future)
            buildConfigField("String", "BASE_URL", "\"https://api.trainingplanner.com/\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(projects.shared)
    implementation(projects.exercise)
    implementation(projects.training)
    implementation(projects.session)
    implementation(projects.analytics)
    implementation(projects.auth)
    implementation(projects.user)
    implementation(projects.network)

    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodelKtx)
    implementation(libs.androidx.activityKtx)
    implementation(libs.timber)
    implementation(libs.coil.compose)
    implementation(libs.bytebeats.charts)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material.icons.extended)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.lifecycleViewmodelCompose)
    implementation(libs.calvin.reorderable)

    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.paging.common)

    // navigation3 libraries
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    // koin
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)

    // Needed for the themes.xml
    implementation(libs.google.material)

    testImplementation(libs.kotlinTestJunit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.paging.testing)

    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.kotlinTestJunit)
    androidTestImplementation(libs.mockk.android)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
