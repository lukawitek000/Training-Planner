plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

//dependencies {
//    implementation(libs.android.gradle.plugin)
//    implementation(libs.kotlin.gradle.plugin)
//    implementation(libs.ksp.gradle.plugin)
//}


dependencies {
    implementation(
        libs.plugins.android.application.get().let {
            "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
        }
    )

    implementation(
        libs.plugins.android.library.get().let {
            "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
        }
    )

    implementation(
        libs.plugins.kotlin.android.get().let {
            "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
        }
    )

    implementation(
        libs.plugins.ksp.get().let {
            "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
        }
    )
}
