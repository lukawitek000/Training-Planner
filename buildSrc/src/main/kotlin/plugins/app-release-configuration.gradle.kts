package plugins

import ConfigData
import com.android.build.api.dsl.ApplicationExtension
import org.gradle.kotlin.dsl.configure
import java.io.File
import java.util.Properties

plugins {
    id("com.android.application")
}

val gitCommitCount = providers.of(GitCommitCountValueSource::class) {
    parameters.rootDir.set(rootProject.layout.projectDirectory)
}.get()

val resolvedVersionCode = providers.gradleProperty("versionCodeOverride").map { it.toInt() }.orNull
    ?: ConfigData.calculateVersionCode(gitCommitCount)
val resolvedVersionName = providers.gradleProperty("versionNameOverride").orNull
    ?: ConfigData.calculateVersionName(gitCommitCount)

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

val releaseStoreFilePath = providers.environmentVariable("SIGNING_STORE_FILE").orNull
    ?: keystoreProperties.getProperty("storeFile")
val releaseStorePassword = providers.environmentVariable("SIGNING_STORE_PASSWORD").orNull
    ?: keystoreProperties.getProperty("storePassword")
val releaseKeyAlias = providers.environmentVariable("SIGNING_KEY_ALIAS").orNull
    ?: keystoreProperties.getProperty("keyAlias")
val releaseKeyPassword = providers.environmentVariable("SIGNING_KEY_PASSWORD").orNull
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
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            resValue("string", "app_name", "Training Planner (Debug)")
        }

        getByName("release") {
            resValue("string", "app_name", "Training Planner")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            val isCi = providers.environmentVariable("CI").isPresent
            val hasAnySigningEnv = providers.environmentVariable("SIGNING_STORE_FILE").isPresent ||
                providers.environmentVariable("SIGNING_STORE_PASSWORD").isPresent ||
                providers.environmentVariable("SIGNING_KEY_ALIAS").isPresent ||
                providers.environmentVariable("SIGNING_KEY_PASSWORD").isPresent

            if (hasValidReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            } else if (isCi && hasAnySigningEnv) {
                logger.warn("WARNING: Incomplete release signing environment variables detected on CI. Release build will remain unsigned.")
            } else {
                logger.info("Release signing credentials not found; release build will remain unsigned or use default fallback.")
            }
        }
    }

    buildFeatures {
        resValues = true
    }
}
