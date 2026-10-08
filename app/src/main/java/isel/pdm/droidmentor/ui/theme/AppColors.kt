package isel.pdm.droidmentor.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min

/**
 * Represents the color specifications for the gradient glow effect.
 *
 * @property glow The primary glow color.
 * @property rawAccent The accent tied to this glow, before it is adjusted for contrast
 */
data class GlowSpec(
    val glow: Color,
    val rawAccent: Color = glow
)

/**
 * Represents the color scheme for the application.
 *
 * @property top The top glow specification.
 * @property bottom The bottom glow specification.
 * @property background The background color.
 */
data class AppColors(
    val top: GlowSpec,
    val bottom: GlowSpec,
    val background: Color,
) {
    val isLight = background.isLightColor()

    // Accents adjusted to be readable on the background
    val topAccent = top.rawAccent.readableOn(background)
    val bottomAccent = bottom.rawAccent.readableOn(background)
    val accent = topAccent
    val onAccent = accent.contentColor()

    // Surface slightly tinted by the accent, so it looks like the glow is reflected on it.
    val surface =
        if (isLight) lerp(lerp(background, Color.White, 0.6f), topAccent, 0.04f)
        else lerp(lerp(background, Color.White, 0.04f), topAccent, 0.04f)
    val accentContainer = lerp(surface, topAccent, 0.25f)

    val border = topAccent.copy(alpha = 0.18f)
    val borderSubtle = (if (isLight) Color.Black else Color.White).copy(alpha = 0.07f)
    val textPrimary = if (isLight) Color(0xFF111411) else Color(0xFFF1F4F1)
    val textSecondary = textPrimary.copy(alpha = 0.6f)

    val crossGradient = Brush.linearGradient(listOf(bottomAccent, topAccent))

    /** Black or white, readable along the whole [crossGradient]. */
    val onCrossGradient = bestContentColorOn(listOf(bottomAccent, topAccent))
    val glowAlpha = if (isLight) 0.45f else 0.25f
}




/** Gives every composable access to the current [AppColors]. */
val LocalAppColors = staticCompositionLocalOf { DefaultDarkColors }
