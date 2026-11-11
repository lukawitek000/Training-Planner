package com.lukasz.witkowski.training.planner.image.infrastructure

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import coil3.ImageLoader
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import coil3.svg.SvgDecoder
import com.lukasz.witkowski.training.planner.shared.theme.TrainingPlannerColorScheme
import okio.Buffer
import okio.ByteString.Companion.encodeUtf8

/**
 * A theme-aware SVG decoder that dynamically maps semantic color tokens in standard SVGs
 * to the active ColorScheme (Material 3).
 *
 * Semantic Token Contract:
 * - `#111111` -> `onSurface` (Mannequin body lines)
 * - `#FF0000` -> `primary` (Active target muscles)
 * - `#0000FF` -> `tertiary` (Secondary/stabilizer muscles)
 * - `#888888` -> `outline` (Apparatus / equipment / floor)
 */
class ThemeAwareSvgDecoder(
    private val source: ImageSource,
    private val options: Options,
    private val useViewBoundsAsIntrinsicSize: Boolean = true,
    private val bodyColorHex: String = TrainingPlannerColorScheme.onSurface.toHex(),
    private val primaryColorHex: String = TrainingPlannerColorScheme.primary.toHex(),
    private val secondaryColorHex: String = TrainingPlannerColorScheme.tertiary.toHex(),
    private val apparatusColorHex: String = TrainingPlannerColorScheme.outline.toHex()
) : Decoder {

    override suspend fun decode(): DecodeResult? {
        val rawSvg = source.source().readUtf8()
        val themedSvg = rawSvg
            .replace("#111111", bodyColorHex)
            .replace("#FF0000", primaryColorHex)
            .replace("#0000FF", secondaryColorHex)
            .replace("#888888", apparatusColorHex)

        val buffer = Buffer().write(themedSvg.encodeUtf8())
        val modifiedSource = ImageSource(source = buffer, fileSystem = options.fileSystem)
        val delegate = SvgDecoder(
            source = modifiedSource,
            options = options,
            useViewBoundsAsIntrinsicSize = useViewBoundsAsIntrinsicSize
        )
        return delegate.decode()
    }

    class Factory(
        private val useViewBoundsAsIntrinsicSize: Boolean = true,
        private val colorScheme: ColorScheme = TrainingPlannerColorScheme
    ) : Decoder.Factory {

        private val bodyColorHex = colorScheme.onSurface.toHex()
        private val primaryColorHex = colorScheme.primary.toHex()
        private val secondaryColorHex = colorScheme.tertiary.toHex()
        private val apparatusColorHex = colorScheme.outline.toHex()

        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder? {
            val isSvg = result.mimeType == MIME_TYPE_SVG ||
                result.source.fileOrNull()?.name?.endsWith(".svg", ignoreCase = true) == true ||
                isSvg(result.source.source())

            if (!isSvg) return null

            return ThemeAwareSvgDecoder(
                source = result.source,
                options = options,
                useViewBoundsAsIntrinsicSize = useViewBoundsAsIntrinsicSize,
                bodyColorHex = bodyColorHex,
                primaryColorHex = primaryColorHex,
                secondaryColorHex = secondaryColorHex,
                apparatusColorHex = apparatusColorHex
            )
        }

        private fun isSvg(source: okio.BufferedSource): Boolean =
            source.rangeEquals(0, SVG_TAG) || source.rangeEquals(0, XML_TAG)

        private companion object {
            const val MIME_TYPE_SVG = "image/svg+xml"
            val SVG_TAG = "<svg".encodeUtf8()
            val XML_TAG = "<?xml".encodeUtf8()
        }
    }

    companion object {
        fun Color.toHex(): String {
            val red = (this.red * 255).toInt().coerceIn(0, 255)
            val green = (this.green * 255).toInt().coerceIn(0, 255)
            val blue = (this.blue * 255).toInt().coerceIn(0, 255)
            return String.format("#%02X%02X%02X", red, green, blue)
        }
    }
}
