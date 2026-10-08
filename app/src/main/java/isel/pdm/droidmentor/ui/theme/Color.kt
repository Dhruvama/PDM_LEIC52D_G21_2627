package isel.pdm.droidmentor.ui.theme

import android.graphics.Color.colorToHSV
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import kotlin.math.max
import kotlin.math.min

/** Luminance above which a background counts as light. */
const val LIGHT_LUMINANCE_THRESHOLD = 0.3f

/** True if this color is light enough to need dark content on top of it. */
fun Color.isLightColor() = luminance() > LIGHT_LUMINANCE_THRESHOLD

/** Contrast ratio between two colors (1 = same, 21 = black on white).
 * @param a The first color.
 * @param b The second color.
 * @return The contrast ratio between the two colors.
 */
private fun contrastRatio(a: Color, b: Color): Float {
    val la = a.luminance()
    val lb = b.luminance()
    return (max(la, lb) + 0.05f) / (min(la, lb) + 0.05f)
}

/**
 * Black or white, whichever stays more readable on all the given backgrounds
 * (the one whose worst contrast is higher). Useful for content over a gradient.
 */
fun bestContentColorOn(backgrounds: List<Color>): Color {
    fun worstContrast(content: Color) = backgrounds.minOf { contrastRatio(content, it) }
    return if (worstContrast(Color.Black) >= worstContrast(Color.White)) Color.Black
    else Color.White
}

/** Black or white, whichever has more contrast on this color. */
fun Color.contentColor(): Color = bestContentColorOn(listOf(this))

/**
 * Returns this color, darkened (on light backgrounds) or lightened (on dark ones)
 * just enough to reach [minContrast] against [background].
 * @param background The background color.
 * @param minContrast The minimum contrast ratio required.
 * @return The color that meets the contrast requirement.
 */
internal fun Color.readableOn(
    background: Color,
    minContrast: Float = 3f
): Color {
    val target = if (background.isLightColor()) Color.Black else Color.White
    var c = this
    var steps = 0
    while (contrastRatio(c, background) < minContrast && steps < 20) {
        c = lerp(c, target, 0.08f)
        steps++
    }
    return c
}

/** More saturation and a minimum brightness, so the glows from the Material theme stand out.
 * @param extraSaturation The additional saturation to add.
 * @return The vivid color.
 */
internal fun Color.vivid(extraSaturation: Float = 0.3f): Color {
    val (h, s, v) = toHsv()
    return Color.hsv(
        h,
        (s + extraSaturation).coerceAtMost(1f),
        v.coerceAtLeast(0.6f)
    )
}

/** This color as [hue, saturation, value]. */
internal fun Color.toHsv(): FloatArray =
    FloatArray(3).also { colorToHSV(toArgb(), it) }

/**
 * Turns this color into a background for the given mode, keeping its hue.
 * Saturation is clamped to [BackgroundLimits] and brightness set to the mode's default.
 * @param light True for a light background, false for a dark one.
 * @return The background color.
 */
fun Color.toBackground(light: Boolean): Color {
    val hsv = FloatArray(3)
    colorToHSV(toArgb(), hsv)
    return Color.hsv(
        hsv[0],
        hsv[1].coerceIn(BackgroundLimits.saturation(light)),
        if (light) 0.97f else 0.04f
    )
}
