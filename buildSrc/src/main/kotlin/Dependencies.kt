object Dependencies {
    val timber by lazy { "com.jakewharton.timber:timber:${Versions.timber}" }
    val roomRuntime by lazy { "androidx.room:room-runtime:${Versions.room}" }
    val roomKtx by lazy { "androidx.room:room-ktx:${Versions.room}" }
    val roomCompiler by lazy { "androidx.room:room-compiler:${Versions.room}" }
    val coroutines by lazy { "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}" }
    val detekt by lazy { "io.gitlab.arturbosch.detekt:detekt-formatting:${Versions.detekt}" }
    val junit by lazy { "junit:junit:${Versions.junit}" }
    val roboelectric by lazy { "org.robolectric:robolectric:${Versions.roboelectric}" }
    val kotlinTestJunit by lazy { "org.jetbrains.kotlin:kotlin-test-junit:${Versions.kotlin}" }

    object AndroidX {
        val testCore by lazy { "androidx.test:core:${Versions.testCore}" }
        val lifecycleLivedataKtx by lazy { "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}" }
    }
}
