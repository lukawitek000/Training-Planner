package plugins

import ConfigData
import com.android.build.api.dsl.ApplicationExtension
import org.gradle.kotlin.dsl.configure
import java.io.File
import java.util.Properties

plugins {
    id("com.android.application")
}

val gitCommitCount = providers.of(GitCommitCountValueSource::class) {}.get()

val resolvedVersionCode = (project.findProperty("versionCodeOverride") as? String)?.toIntOrNull()
    ?: ConfigData.calculateVersionCode(gitCommitCount)
val resolvedVersionName = (project.findProperty("versionNameOverride") as? String)
    ?: ConfigData.calculateVersionName(gitCommitCount)

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

extensions.configure<ApplicationExtension> {
    defaultConfig {
        versionCode = resolvedVersionCode
        versionName = resolvedVersionName
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
        named("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            resValue("string", "app_name", "Training Planner (Debug)")
        }
        named("release") {
            resValue("string", "app_name", "Training Planner")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            if (hasValidReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            } else {
                logger.info("Release signing credentials not found; release build will remain unsigned or use default fallback.")
            }
        }
    }

    buildFeatures {
        resValues = true
    }
}
