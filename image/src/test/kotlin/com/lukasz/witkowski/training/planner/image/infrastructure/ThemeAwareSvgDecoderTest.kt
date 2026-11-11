package com.lukasz.witkowski.training.planner.image.infrastructure

import android.content.Context
import coil3.decode.DecodeResult
import coil3.decode.ImageSource
import coil3.request.Options
import kotlin.test.assertNotNull
import kotlinx.coroutines.runBlocking
import okio.Buffer
import okio.FileSystem
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ThemeAwareSvgDecoderTest {
    private val context: Context by lazy { RuntimeEnvironment.getApplication() }

    @Test
    fun verifyThemeAwareSvgDecoderDecodesWithReplacedThemeTokens() {
        runBlocking {
            val sampleExerciseSvg = """
                <svg viewBox="0 0 512 512" xmlns="http://www.w3.org/2000/svg">
                    <!-- Floor apparatus -->
                    <path stroke="#888888" stroke-width="4" d="M 50,400 L 462,400" />
                    <!-- Body contour -->
                    <circle cx="256" cy="200" r="40" stroke="#111111" stroke-width="6" fill="none" />
                    <!-- Target muscle working -->
                    <path fill="#FF0000" stroke="#FF0000" stroke-width="4" d="M 230,220 L 280,220 Z" />
                </svg>
            """.trimIndent()

            val buffer = Buffer().writeUtf8(sampleExerciseSvg)
            val imageSource = ImageSource(source = buffer, fileSystem = FileSystem.SYSTEM)
            val options = Options(context)

            val decoder = ThemeAwareSvgDecoder(
                source = imageSource,
                options = options,
                bodyColorHex = "#ABCDEF",
                primaryColorHex = "#123456",
                apparatusColorHex = "#654321"
            )

            val result: DecodeResult? = decoder.decode()
            assertNotNull(result)
            assertNotNull(result.image)
        }
    }
}
