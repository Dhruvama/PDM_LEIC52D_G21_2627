package isel.pdm.droidmentor.activechat.components.particlelogo

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas as BitmapCanvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RadialGradientShader

/** Width and height (px) of the glow sprite bitmap. */
internal const val GLOW_SPRITE_PX = 32

/** One glow sprite per logo color: [main] for most dots, [second] for every 3rd, [highlight] for the front. */
internal class GlowSprites(
    val main: ImageBitmap,
    val second: ImageBitmap,
    val highlight: ImageBitmap,
)

/**
 * Builds a soft round dot of [color]: solid in the center, fading to transparent at the edge.
 * Drawn once into a bitmap, so each frame only copies it instead of computing a gradient.
 */
internal fun makeGlowSprite(color: Color, sizePx: Int = GLOW_SPRITE_PX): ImageBitmap {
    val bitmap = ImageBitmap(sizePx, sizePx)
    val canvas = BitmapCanvas(bitmap)
    val center = Offset(sizePx / 2f, sizePx / 2f)
    val paint = Paint()
    paint.shader = RadialGradientShader(
        center = center,
        radius = sizePx / 2f,
        colors = listOf(color, color.copy(alpha = 0.55f), Color.Transparent),
        colorStops = listOf(0f, 0.35f, 1f),
    )
    canvas.drawCircle(center = center, radius = sizePx / 2f, paint = paint)
    return bitmap
}
