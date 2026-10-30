package isel.pdm.droidmentor.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * Two independent colors for one side (top or bottom) of the theme.
 *
 * @property glow The color of the glow drawn in the background.
 * @property rawAccent The accent used by UI elements, before it is adjusted for contrast.
 * Starts equal to [glow] and follows it while linked in the settings.
 */
data class ColorPair(
    val glow: Color,
    val rawAccent: Color = glow
)

/**
 * Represents the color scheme for the application.
 *
 * @property top The top glow and accent colors.
 * @property bottom The bottom glow and accent colors.
 * @property background The background color.
 */
data class AppColors(
    val top: ColorPair,
    val bottom: ColorPair,
    val background: Color,
) {
    val isLight = background.isLightColor()

    // Accents adjusted to be readable on the background
    val topAccent = top.rawAccent.readableOn(background)
    val bottomAccent = bottom.rawAccent.readableOn(background)

    /** The app's highlight color (Material primary). */
    val accent = topAccent
    val onAccent = accent.contentColor()

    // Slightly tinted by the accent, as if the glow reflected on it.
    val surface =
        if (isLight) lerp(lerp(background, Color.White, 0.6f), topAccent, 0.04f)
        else lerp(lerp(background, Color.White, 0.04f), topAccent, 0.04f)
    val accentContainer = lerp(surface, topAccent, 0.25f)

    val border = topAccent.copy(alpha = 0.18f)
    val borderSubtle = (if (isLight) Color.Black else Color.White).copy(alpha = 0.07f)
    // Near-black / near-white instead of pure black / white.
    val textPrimary = if (isLight) Color(0xFF111411) else Color(0xFFF1F4F1)
    val textSecondary = textPrimary.copy(alpha = 0.6f)

    val crossGradient = Brush.linearGradient(listOf(bottomAccent, topAccent))

    /** Black or white, readable along the whole [crossGradient]. */
    val onCrossGradient = bestContentColorOn(listOf(bottomAccent, topAccent))

    // A glow shows less on a light background, so it needs more opacity there.
    val glowAlpha = if (isLight) 0.45f else 0.25f
}




/** Gives every composable access to the current [AppColors]. */
val LocalAppColors = staticCompositionLocalOf { DefaultDarkColors }
