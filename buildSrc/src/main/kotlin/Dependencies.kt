object BuildPlugins {
    val android by lazy { "com.android.tools.build:gradle:${Versions.gradlePlugin}" }
    val kotlin by lazy { "com.android.tools.build:gradle:${Versions.kotlin}" }
    val hilt by lazy { "com.google.dagger:hilt-android-gradle-plugin:${Versions.gradlePlugin}" }
}

object Dependencies {

}